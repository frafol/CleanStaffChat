package it.frafol.cleanstaffchat.velocity;

import com.google.inject.Inject;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import it.frafol.cleanstaffchat.velocity.channel.ChannelCommand;
import it.frafol.cleanstaffchat.velocity.channel.ChannelListener;
import it.frafol.cleanstaffchat.velocity.channel.ChannelMuteCommand;
import it.frafol.cleanstaffchat.velocity.channel.ChannelToggleCommand;
import it.frafol.cleanstaffchat.velocity.channel.ChatChannel;
import it.frafol.cleanstaffchat.velocity.objects.DiscordBridge;
import it.frafol.cleanstaffchat.velocity.enums.*;
import it.frafol.cleanstaffchat.velocity.general.commands.MuteChatCommand;
import it.frafol.cleanstaffchat.velocity.hooks.RedisListener;
import it.frafol.cleanstaffchat.velocity.objects.JdaBuilder;
import it.frafol.cleanstaffchat.velocity.objects.TextFile;
import it.frafol.cleanstaffchat.velocity.staffchat.commands.*;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.StaffListDiscordListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ServerListener;
import it.frafol.cleanstaffchat.velocity.utils.LibertyBansUtil;
import it.frafol.cleanstaffchat.velocity.utils.LiteBansUtil;
import it.frafol.cleanstaffchat.velocity.utils.SwitchUtil;
import it.frafol.cleanstaffchat.velocity.utils.VanishUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import net.byteflux.libby.relocation.Relocation;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

@Getter
@Plugin(
        id = "cleanstaffchat",
        name = "CleanStaffChat",
        version = "2.0.0",
        dependencies = {@Dependency(id = "redisbungee", optional = true), @Dependency(id = "unsignedvelocity", optional = true), @Dependency(id = "signedvelocity", optional = true), @Dependency(id = "spicord", optional = true), @Dependency(id = "leaf", optional = true), @Dependency(id = "miniplaceholders", optional = true), @Dependency(id = "clientcatcher", optional = true)},
        url = "github.com/frafol",
        authors = "frafol"
)
public class CleanStaffChat {

    private final Metrics.Factory metricsFactory;
    private final ProxyServer server;
    private final Logger logger;

    private final Path path;
    private JdaBuilder jda;

    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile redisTextFile;
    private TextFile serversTextFile;
    private TextFile versionTextFile;

    public static final ChannelIdentifier channel = MinecraftChannelIdentifier.create("cleansc", "cancel");

    @Getter
    private static CleanStaffChat instance;

    @Getter
    private boolean update = false;

    @Getter
    private String updatedVersion = "";

    public boolean updated = false;

    @Inject
    public CleanStaffChat(ProxyServer server, Logger logger, @DataDirectory Path path, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.path = path;
        this.metricsFactory = metricsFactory;
    }

    @Inject
    public PluginContainer container;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        instance = this;
        loadLibraries();

        getLogger().info("""
                
                  ___  __    ____    __    _  _    ___   ___\s
                 / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)
                ( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__\s
                 \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)
                """);

        logger.info("Server version: {}", getServer().getVersion().getVersion());

        loadFiles();
        updateConfig();
        getLogger().info("Configurations loaded successfully!");

        if (VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
            getServer().getScheduler().buildTask(this, this::UpdateChecker).repeat(1, TimeUnit.HOURS).schedule();
        }

        startJDA();

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("screload")
                .aliases("staffchatreload", "staffreload", "cleanscreload", "cleanstaffchatreload")
                .build(), new ReloadCommand(this));

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("scdebug")
                .aliases("staffchatdebug", "staffdebug", "cleanscdebug", "cleanstaffchatdebug")
                .build(), new DebugCommand(this));

        server.getEventManager().register(this, new DebugCommand(this));

        if (VelocityConfig.DOUBLE_MESSAGE.get(Boolean.class)) {
            loadChannelRegistrar();
        }

        if (VelocityConfig.STAFFLIST_MODULE.get(Boolean.class)) {
            registerStaffList();
        }

        for (ChatChannel channelEntry : ChatChannel.values()) {
            registerChannel(channelEntry);
        }
        registerStaffExtras();

        if (VelocityConfig.MUTECHAT_MODULE.get(Boolean.class)) {
            registerMuteChat();
        }

        if (VelocityRedis.REDIS_ENABLE.get(Boolean.class) && !getRedisBungee()) {
            getLogger().error("RedisBungee was not found, the RedisBungee hook won't work.");
        }

        if (VelocityRedis.REDIS_ENABLE.get(Boolean.class) && getRedisBungee()) {
            registerRedisBungee();
            getLogger().info("Hooked into RedisBungee successfully!");
        }

        if (isPremiumVanish()) {
            getLogger().info("Hooked into PremiumVanish successfully!");
        }

        if (getMiniPlaceholders()) {
            getLogger().info("Hooked into MiniPlaceholders successfully!");
        }

        if (VelocityConfig.STATS.get(Boolean.class)) {
            metricsFactory.make(this, 16447);
            getLogger().info("Metrics loaded successfully!");
        }

        if (!getUnsignedVelocityAddon() && !getSignedVelocity()) {
            getLogger().warn("If you get kicked out in 1.19+ while typing in a staffchat on Velocity, " +
                    "consider downloading plugins like unSignedVelocity or SignedVelocity (Recommended).");
        }

        getLogger().info("Plugin successfully enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) throws LoginException {
        logger.info("Deleting instances...");
        server.getChannelRegistrar().unregister(channel);

        if (jda != null) {
            jda.JdaWorker().shutdownNow();
        }

        instance = null;
        configTextFile = null;
        logger.info("Successfully disabled.");
    }

    private void loadFiles() {
        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");
        discordTextFile = new TextFile(path, "discord.yml");
        aliasesTextFile = new TextFile(path, "aliases.yml");
        redisTextFile = new TextFile(path, "redis.yml");
        serversTextFile = new TextFile(path, "servers.yml");
        versionTextFile = new TextFile(path, "version.yml");
    }

    private void loadLibraries() {
        VelocityLibraryManager<CleanStaffChat> velocityLibraryManager = new VelocityLibraryManager<>(getLogger(), path, getServer().getPluginManager(), this);

        Library yaml;
        final Relocation yamlrelocation = new Relocation("yaml", "it{}frafol{}libs{}yaml");
        yaml = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.4")
                .url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
                .relocate(yamlrelocation)
                .build();

        final Relocation updaterrelocation = new Relocation("updater", "it{}frafol{}libs{}updater");
        Library updater = Library.builder()
                .groupId("ru{}vyarus")
                .artifactId("yaml-config-updater")
                .version("1.4.2")
                .relocate(updaterrelocation)
                .build();

        final Relocation kotlin = new Relocation("kotlin", "it{}frafol{}libs{}kotlin");
        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("6.7.0")
                .relocate(kotlin)
                .url("https://github.com/discord-jda/JDA/releases/download/v6.7.0/JDA-6.7.0-withDependencies-min.jar")
                .build();

        velocityLibraryManager.addMavenCentral();
        velocityLibraryManager.addJitPack();
        velocityLibraryManager.loadLibrary(yaml);
        velocityLibraryManager.loadLibrary(updater);

        try {
            Class.forName("net.dv8tion.jda.api.entities.Member");
        } catch (ClassNotFoundException ignored) {
            velocityLibraryManager.loadLibrary(discord);
        }
    }

    public void startJDA() {
        if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            removeFallbackLogger();
            jda = new JdaBuilder();
            jda.startJDA();
            updateJDATask();
            getLogger().info("Hooked into Discord successfully!");
        }
    }

    private void removeFallbackLogger() {
        try {
            JDALogger.setFallbackLoggerEnabled(false);
        } catch (NoSuchMethodError ignored) {}
    }

    private void UpdateChecker() {
        if (container.getDescription().getVersion().isEmpty()) return;
        new UpdateCheck(this).getVersion(version -> {
            if (container.getDescription().getVersion().isEmpty()) return;
            if (Integer.parseInt(container.getDescription().getVersion().get().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {
                if (VelocityConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
                    autoUpdate();
                    return;
                }
                if (!updated) {
                    update = true;
                    updatedVersion = version;
                    logger.warn("There is a new update available, download it on SpigotMC!");
                }
            }
            if (Integer.parseInt(container.getDescription().getVersion().get().replace(".", "")) > Integer.parseInt(version.replace(".", ""))) {
                logger.warn("You are using a development version, please report any bugs!");
            }
        });
    }

    @SneakyThrows
    private void updateConfig() {
        if (container.getDescription().getVersion().isPresent() && (!container.getDescription().getVersion().get().equals(VelocityVersion.VERSION.get(String.class)))) {
            logger.info("Creating new §dconfigurations...");
            YamlUpdater.create(new File(path + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/main/velocity/src/main/resources/config.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/main/velocity/src/main/resources/messages.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/discord.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/main/velocity/src/main/resources/discord.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/redis.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/main/velocity/src/main/resources/redis.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/aliases.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/main/velocity/src/main/resources/aliases.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/servers.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/main/velocity/src/main/resources/servers.yml"))
                    .backup(true)
                    .update();
            versionTextFile.getConfig().set("version", container.getDescription().getVersion().get());
            versionTextFile.getConfig().save();
            loadFiles();
        }
    }

    @SuppressWarnings("removal")
    private void registerRedisBungee() {
        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
        for (ChatChannel channelEntry : ChatChannel.values()) {
            redisBungeeAPI.registerPubSubChannels(channelEntry.redisMessageChannel);
            redisBungeeAPI.registerPubSubChannels(channelEntry.redisMuteChannel);
        }
        redisBungeeAPI.registerPubSubChannels(RedisListener.STAFF_AFK_CHANNEL);
        redisBungeeAPI.registerPubSubChannels(RedisListener.STAFF_OTHER_CHANNEL);
        server.getEventManager().register(this, new RedisListener(this));
    }

    @SneakyThrows
    private void registerStaffList() {

        if (!getLuckPerms()) {
            logger.warn("You need LuckPermsVelocity to use StaffList.");
            return;
        }

        if (VelocityCommandsConfig.STAFFLIST.getStringList() == null) return;
        final String[] aliases_stafflist = VelocityCommandsConfig.STAFFLIST.getStringList().toArray(new String[0]);
        if (aliases_stafflist.length == 0) return;
        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.STAFFLIST.getStringList().get(0))
                .aliases(aliases_stafflist)
                .build(), new StaffListCommand(this));
    }

    @SneakyThrows
    private void registerChannel(ChatChannel channelEntry) {
        if (!channelEntry.isEnabled()) return;
        registerCommand(channelEntry.aliases.main, labels -> new ChannelCommand(this, channelEntry));
        registerCommand(channelEntry.aliases.mute, labels -> new ChannelMuteCommand(channelEntry));
        registerCommand(channelEntry.aliases.toggle, labels -> new ChannelToggleCommand(channelEntry));
        final ChannelListener listener = new ChannelListener(this, channelEntry);
        server.getEventManager().register(this, listener);
        if (jda != null && DiscordBridge.isActive(channelEntry.settings.discordModule)) {
            jda.JdaWorker().addEventListener(listener);
        }
    }

    private void registerStaffExtras() {
        if (!ChatChannel.STAFF.isEnabled()) return;
        registerCommand(VelocityCommandsConfig.STAFFCHAT_AFK, labels -> new it.frafol.cleanstaffchat.velocity.staffchat.commands.AFKCommand());
        server.getEventManager().register(this, new JoinListener(this));
        server.getEventManager().register(this, new ServerListener(this));
        if (jda != null && DiscordBridge.isActive(VelocityConfig.STAFFCHAT_DISCORD_MODULE)) {
            jda.JdaWorker().addEventListener(new StaffListDiscordListener(this));
        }
    }

    private void registerCommand(VelocityCommandsConfig alias, java.util.function.Function<java.util.List<String>, com.velocitypowered.api.command.SimpleCommand> factory) {
        final java.util.List<String> labels = alias.getStringList();
        if (labels.isEmpty()) return;
        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(labels.get(0)).aliases(labels.subList(1, labels.size()).toArray(new String[0])).build(),
                factory.apply(labels));
    }

    private void registerMuteChat() {
        if (VelocityCommandsConfig.MUTECHAT.getStringList() == null) return;
        final String[] aliases = VelocityCommandsConfig.MUTECHAT.getStringList().toArray(new String[0]);
        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.MUTECHAT.getStringList().get(0))
                .aliases(aliases)
                .build(), new MuteChatCommand(this));
        server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.general.listeners.ChatListener(this));
    }

    private void updateJDATask() {
        getServer().getScheduler().buildTask(this, () -> {
            try {
                updateJDA();
            } catch (LoginException e) {
                throw new RuntimeException("Fatal error while updating JDA. Is Discord Bot configured correctly?" , e);
            }
        }).repeat(30, TimeUnit.SECONDS).schedule();
    }

    public void updateJDA() throws LoginException {
        if (!VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) return;
        if (jda.getJda() == null) {
            logger.error("Fatal error while updating JDA. Is Discord Bot configured correctly?");
            return;
        }
        String activity = VelocityDiscordConfig.DISCORD_ACTIVITY.get(String.class);
        String activityType = VelocityDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class);
        if (activity.isEmpty() || activityType.equals("NULL")) return;
        if (isPremiumVanish()) {
            jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
                            (VelocityDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()), activity
                            .replace("%players%", String.valueOf(server.getAllPlayers().size() - VanishUtil.getVanishedPlayers().size()))));
            return;
        }
        jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
                        (VelocityDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()), activity
                        .replace("%players%", String.valueOf(server.getAllPlayers().size()))));
    }

    public void autoUpdate() {
        try {
            String fileUrl = "https://github.com/frafol/CleanStaffChat/releases/download/release/CleanStaffChat.jar";
            String destination = "./plugins/";
            String fileName = getFileNameFromUrl(fileUrl);
            File outputFile = new File(destination, fileName);
            downloadFile(fileUrl, outputFile);
            updated = true;
            logger.warn("CleanStaffChat successfully updated, a restart is required.");
        } catch (IOException ignored) {
            logger.error("Error while updating CleanStaffChat, please report this error on https://dsc.gg/futuredevelopment.");
        }
    }

    private String getFileNameFromUrl(String fileUrl) {
        int slashIndex = fileUrl.lastIndexOf('/');
        if (slashIndex != -1 && slashIndex < fileUrl.length() - 1) return fileUrl.substring(slashIndex + 1);
        throw new IllegalArgumentException("Invalid file URL");
    }

    private void downloadFile(String fileUrl, File outputFile) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream inputStream = url.openStream()) {
            Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean isPremiumVanish() {
        if (VelocityConfig.PREMIUMVANISH.get(Boolean.class)) return getServer().getPluginManager().isLoaded("premiumvanish");
        return false;
    }

    private boolean getLuckPerms() {
        return getServer().getPluginManager().isLoaded("luckperms");
    }

    private boolean getRedisBungee() {
        return getServer().getPluginManager().isLoaded("redisbungee");
    }

    private boolean getUnsignedVelocityAddon() {
        return getServer().getPluginManager().isLoaded("unsignedvelocity") || getServer().getPluginManager().isLoaded("unsignedvelocity2");
    }

    private boolean getSignedVelocity() {
        return getServer().getPluginManager().isLoaded("signedvelocity");
    }

    private boolean getSpicord() {
        return getServer().getPluginManager().isLoaded("spicord");
    }

    public boolean getMiniPlaceholders() {
        return getServer().getPluginManager().isLoaded("miniplaceholders") && VelocityConfig.MINIPLACEHOLDERS.get(Boolean.class);
    }
    private boolean getLiteBans() {
        return getServer().getPluginManager().isLoaded("litebans");
    }

    private boolean getLibertyBans() {
        return getServer().getPluginManager().isLoaded("libertybans");
    }

    public boolean isMuted(Player player) {
        if (getLiteBans()) return LiteBansUtil.isMuted(player.getUniqueId(), player.getRemoteAddress().toString());
        if (getLibertyBans()) return LibertyBansUtil.isMuted(player.getUniqueId());
        return false;
    }

    private void loadChannelRegistrar() {
        server.getChannelRegistrar().register(channel);
        server.getEventManager().register(this, new SwitchUtil(this));
    }

    public boolean isInBlockedStaffChatServer(Player player) {
        return ChatChannel.STAFF.isBlocked(player);
    }

    public boolean isInBlockedAdminChatServer(Player player) {
        return ChatChannel.ADMIN.isBlocked(player);
    }

    public boolean isInBlockedDonorChatServer(Player player) {
        return ChatChannel.DONOR.isBlocked(player);
    }
}