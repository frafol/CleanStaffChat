package it.frafol.cleanstaffchat.velocity;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.VelocityLibraryManager;
import com.alessiodp.libby.relocation.Relocation;
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
import it.frafol.cleanstaffchat.velocity.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.velocity.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.velocity.enums.*;
import it.frafol.cleanstaffchat.velocity.general.commands.MuteChatCommand;
import it.frafol.cleanstaffchat.velocity.hooks.RedisListener;
import it.frafol.cleanstaffchat.velocity.objects.JdaBuilder;
import it.frafol.cleanstaffchat.velocity.objects.TextFile;
import it.frafol.cleanstaffchat.velocity.staffchat.commands.*;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ServerListener;
import it.frafol.cleanstaffchat.velocity.utils.VanishUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        version = "1.16.1",
        dependencies = {@Dependency(id = "redisbungee", optional = true), @Dependency(id = "unsignedvelocity", optional = true), @Dependency(id = "signedvelocity", optional = true), @Dependency(id = "spicord", optional = true), @Dependency(id = "leaf", optional = true), @Dependency(id = "miniplaceholders", optional = true), @Dependency(id = "clientcatcher", optional = true)},
        url = "github.com/frafol",
        authors = "frafol"
)
public class CleanStaffChat {

    @Inject
    private final Metrics.Factory metricsFactory;

    @Inject
    private final ProxyServer server;

    @Inject
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

    @Inject
    public static final ChannelIdentifier channel = MinecraftChannelIdentifier.create("cleansc", "cancel");

    @Getter
    private static CleanStaffChat instance;

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

        getLogger().info("\n  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        logger.info("Server version: " + getServer().getVersion());
        checkIncompatibilities();

        loadFiles();
        updateConfig();
        getLogger().info("Configurations loaded successfully!");

        if (VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
            UpdateChecker();
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

        if (VelocityConfig.STAFFCHAT.get(Boolean.class)) {
            registerStaffChat();
        }

        if (VelocityConfig.DONORCHAT.get(Boolean.class)) {
            registerDonorChat();
        }

        if (VelocityConfig.ADMINCHAT.get(Boolean.class)) {
            registerAdminChat();
        }

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
        getLogger().info("Deleting instances...");
        server.getChannelRegistrar().unregister(channel);

        if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.getJda().shutdownNow();
        }

        instance = null;
        configTextFile = null;
        logger.info("Successfully disabled.");
    }

    private void checkIncompatibilities() {
        if (getSpicord()) {
            logger.error("Spicord found, this plugin is completely unsupported and you won't receive any support.");
        }
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
        VelocityLibraryManager<CleanStaffChat> velocityLibraryManager = new VelocityLibraryManager<>(this, getLogger(), path, getServer().getPluginManager());

        Library yaml;
        final Relocation yamlrelocation = new Relocation("yaml", "it{}frafol{}libs{}yaml");
        yaml = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.4")
                .relocate(yamlrelocation)
                .build();

        final Relocation updaterrelocation = new Relocation("updater", "it{}frafol{}libs{}updater");
        Library updater = Library.builder()
                .groupId("ru{}vyarus")
                .artifactId("yaml-config-updater")
                .version("1.4.2")
                .relocate(updaterrelocation)
                .build();

        // JDA should be beta.18 because of Java 8 incompatibility.
        final Relocation kotlin = new Relocation("discord", "it{}frafol{}libs{}discord");
        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("5.0.0-beta.18")
                .relocate(kotlin)
                .url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.18/JDA-5.0.0-beta.18-withDependencies-min.jar")
                .build();

        velocityLibraryManager.addMavenCentral();
        velocityLibraryManager.addJitPack();

        try {
            velocityLibraryManager.loadLibrary(yaml);
        } catch (RuntimeException ignored) {
            getLogger().error("Failed to load Simple-YAML, trying to download it from GitHub...");
            yaml = Library.builder()
                    .groupId("me{}carleslc{}Simple-YAML")
                    .artifactId("Simple-Yaml")
                    .version("1.8.4")
                    .url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
                    .relocate(yamlrelocation)
                    .build();
        }

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
            jda = new JdaBuilder();
            jda.startJDA();
            updateJDATask();
            getLogger().info("Hooked into Discord successfully!");
        }
    }

    private void UpdateChecker() {

        if (!container.getDescription().getVersion().isPresent()) {
            return;
        }

        new UpdateCheck(this).getVersion(version -> {

            if (Integer.parseInt(container.getDescription().getVersion().get().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {

                if (VelocityConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
                    autoUpdate();
                    return;
                }

                if (!updated) {
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
            YamlUpdater.create(new File(path + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/config.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/messages.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/discord.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/discord.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/redis.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/redis.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/aliases.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/aliases.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(path + "/servers.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/servers.yml"))
                    .backup(true)
                    .update();
            versionTextFile.getConfig().set("version", container.getDescription().getVersion().get());
            versionTextFile.getConfig().save();
            loadFiles();
        }
    }

    public void UpdateCheck(Player player) {
        if (!VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
            return;
        }

        if (!container.getDescription().getVersion().isPresent()) {
            return;
        }

        new UpdateCheck(this).getVersion(version -> {

            if (!(Integer.parseInt(container.getDescription().getVersion().get().replace(".", ""))
                    < Integer.parseInt(version.replace(".", "")))) {
                return;
            }

            if (VelocityConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
                autoUpdate();
                return;
            }

            if (!updated) {
                player.sendMessage(LegacyComponentSerializer.legacy('§')
                        .deserialize(VelocityMessages.UPDATE.color()
                                .replace("%version%", version)
                                .replace("%prefix%", VelocityMessages.PREFIX.color())));
            }

        });
    }

    private void registerRedisBungee() {

        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

        server.getEventManager().register(this, new RedisListener(this));

        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-AdminMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-DonorMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffAFKMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffOtherMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffAFKMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-MuteStaffChat-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-MuteAdminChat-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-MuteDonorChat-RedisBungee");
    }

    @SneakyThrows
    private void registerStaffList() {

        if (!server.getPluginManager().isLoaded("luckperms")) {
            logger.warn("You need LuckPermsVelocity to use StaffList.");
            return;
        }

        if (VelocityCommandsConfig.STAFFLIST.getStringList() == null) {
            return;
        }

        final String[] aliases_stafflist = VelocityCommandsConfig.STAFFLIST.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.STAFFLIST.getStringList().get(0))
                .aliases(aliases_stafflist)
                .build(), new StaffListCommand(this));
    }

    @SneakyThrows
    private void registerStaffChat() {

        if (VelocityCommandsConfig.STAFFCHAT.getStringList() != null) {
            final String[] aliases_staffchat = VelocityCommandsConfig.STAFFCHAT.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT.getStringList().get(0))
                    .aliases(aliases_staffchat)
                    .build(), new StaffChatCommand(this));
        }

        if (VelocityCommandsConfig.STAFFCHAT_MUTE.getStringList() != null) {
            final String[] aliases_staffchatmute = VelocityCommandsConfig.STAFFCHAT_MUTE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT_MUTE.getStringList().get(0))
                    .aliases(aliases_staffchatmute)
                    .build(), new MuteCommand(this));
        }

        if (VelocityCommandsConfig.STAFFCHAT_TOGGLE.getStringList() != null) {
            final String[] aliases_staffchattoggle = VelocityCommandsConfig.STAFFCHAT_TOGGLE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT_TOGGLE.getStringList().get(0))
                    .aliases(aliases_staffchattoggle)
                    .build(), new ToggleCommand(this));
        }

        if (VelocityCommandsConfig.STAFFCHAT_AFK.getStringList() != null) {
            final String[] aliases_staffchatafk = VelocityCommandsConfig.STAFFCHAT_AFK.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT_AFK.getStringList().get(0))
                    .aliases(aliases_staffchatafk)
                    .build(), new AFKCommand(this));
        }

        server.getEventManager().register(this, new JoinListener(this));
        server.getEventManager().register(this, new ServerListener(this));

        if (getLuckPerms()) {
            server.getEventManager().register(this, new ChatListener(this));
        }

        if (VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.getJda().addEventListener(new ChatListener(this));
        }
    }

    @SneakyThrows
    private void registerDonorChat() {

        if (VelocityCommandsConfig.DONORCHAT.getStringList() != null) {
            final String[] aliases_donorchat = VelocityCommandsConfig.DONORCHAT.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.DONORCHAT.getStringList().get(0))
                    .aliases(aliases_donorchat)
                    .build(), new DonorChatCommand(this));
        }

        if (VelocityCommandsConfig.DONORCHAT_MUTE.getStringList() != null) {
            final String[] aliases_donormute = VelocityCommandsConfig.DONORCHAT_MUTE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.DONORCHAT_MUTE.getStringList().get(0))
                    .aliases(aliases_donormute)
                    .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.MuteCommand(this));
        }

        if (VelocityCommandsConfig.DONORCHAT_TOGGLE.getStringList() != null) {
            final String[] aliases_donortoggle = VelocityCommandsConfig.DONORCHAT_TOGGLE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.DONORCHAT_TOGGLE.getStringList().get(0))
                    .aliases(aliases_donortoggle)
                    .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.ToggleCommand(this));
        }

        if (getLuckPerms()) {
            server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));
        }

        if (VelocityConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));
        }
    }

    @SneakyThrows
    private void registerAdminChat() {

        if (VelocityCommandsConfig.ADMINCHAT.getStringList() != null) {
            final String[] aliases_adminchat = VelocityCommandsConfig.ADMINCHAT.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.ADMINCHAT.getStringList().get(0))
                    .aliases(aliases_adminchat)
                    .build(), new AdminChatCommand(this));
        }

        if (VelocityCommandsConfig.ADMINCHAT_MUTE.getStringList() != null) {
            final String[] aliases_adminchatmute = VelocityCommandsConfig.ADMINCHAT_MUTE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.ADMINCHAT_MUTE.getStringList().get(0))
                    .aliases(aliases_adminchatmute)
                    .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.MuteCommand(this));
        }

        if (VelocityCommandsConfig.ADMINCHAT_TOGGLE.getStringList() != null) {
            final String[] aliases_adminchattoggle = VelocityCommandsConfig.ADMINCHAT_TOGGLE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.ADMINCHAT_TOGGLE.getStringList().get(0))
                    .aliases(aliases_adminchattoggle)
                    .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.ToggleCommand(this));
        }

        if (getLuckPerms()) {
            server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));
        }

        if (VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));
        }
    }

    @SneakyThrows
    private void registerMuteChat() {

        if (VelocityCommandsConfig.MUTECHAT.getStringList() == null) {
            return;
        }

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

        if (!VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            return;
        }

        if (jda.getJda() == null) {
            logger.error("Fatal error while updating JDA. Is Discord Bot configured correctly?");
            return;
        }

        if (isPremiumVanish()) {
            jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
                            (VelocityDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                    VelocityDiscordConfig.DISCORD_ACTIVITY.get(String.class)
                            .replace("%players%", String.valueOf(server.getAllPlayers().size() - VanishUtil.getVanishedPlayers().size()))));
            return;
        }

        jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
                        (VelocityDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                VelocityDiscordConfig.DISCORD_ACTIVITY.get(String.class)
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
        if (slashIndex != -1 && slashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(slashIndex + 1);
        }
        throw new IllegalArgumentException("Invalid file URL");
    }

    private void downloadFile(String fileUrl, File outputFile) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream inputStream = url.openStream()) {
            Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean isPremiumVanish() {
        if (VelocityConfig.PREMIUMVANISH.get(Boolean.class)) {
            return getServer().getPluginManager().isLoaded("premiumvanish");
        }
        return false;
    }

    private boolean getLuckPerms() {
        return getServer().getPluginManager().isLoaded("luckperms");
    }

    private boolean getRedisBungee() {
        return getServer().getPluginManager().isLoaded("redisbungee");
    }

    private boolean getUnsignedVelocityAddon() {
        return getServer().getPluginManager().isLoaded("unsignedvelocity");
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

    private void loadChannelRegistrar() {
        server.getChannelRegistrar().register(channel);
    }

    public boolean isInBlockedStaffChatServer(Player player) {
        if (player.getCurrentServer().isPresent()) {
            return (!VelocityServers.SC_BLOCKED_SRV.getStringList().isEmpty() && VelocityServers.SC_BLOCKED_SRV.getStringList().contains(player.getCurrentServer().get().getServerInfo().getName()));
        }
        return false;
    }

    public boolean isInBlockedAdminChatServer(Player player) {
        if (player.getCurrentServer().isPresent()) {
            return (!VelocityServers.AC_BLOCKED_SRV.getStringList().isEmpty() && VelocityServers.AC_BLOCKED_SRV.getStringList().contains(player.getCurrentServer().get().getServerInfo().getName()));
        }
        return false;
    }

    public boolean isInBlockedDonorChatServer(Player player) {
        if (player.getCurrentServer().isPresent()) {
            return (!VelocityServers.DC_BLOCKED_SRV.getStringList().isEmpty() && VelocityServers.DC_BLOCKED_SRV.getStringList().contains(player.getCurrentServer().get().getServerInfo().getName()));
        }
        return false;
    }
}