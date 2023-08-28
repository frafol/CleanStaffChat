package it.frafol.cleanstaffchat.velocity;

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
import it.frafol.cleanstaffchat.velocity.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.velocity.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.velocity.enums.*;
import it.frafol.cleanstaffchat.velocity.hooks.RedisListener;
import it.frafol.cleanstaffchat.velocity.objects.JdaBuilder;
import it.frafol.cleanstaffchat.velocity.objects.TextFile;
import it.frafol.cleanstaffchat.velocity.staffchat.commands.*;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ServerListener;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import net.byteflux.libby.relocation.Relocation;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import javax.inject.Inject;
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
        version = "1.12.1",
        dependencies = {@Dependency(id = "redisbungee", optional = true), @Dependency(id = "unsignedvelocity", optional = true)},
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
    private TextFile versionTextFile;

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

        VelocityLibraryManager<CleanStaffChat> velocityLibraryManager = new VelocityLibraryManager<>(getLogger(), path, getServer().getPluginManager(), this);

        Library yaml;
        yaml = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.4")
                .build();

        Library updater = Library.builder()
                .groupId("ru{}vyarus")
                .artifactId("yaml-config-updater")
                .version("1.4.2")
                .build();

        final Relocation kotlin = new Relocation("kotlin", "it{}frafol{}libs{}kotlin");
        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("5.0.0-beta.12")
                .url("https://github.com/discord-jda/JDA/releases/download/v5.0.0-beta.12/JDA-5.0.0-beta.12-withDependencies-min.jar")
                .relocate(kotlin)
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
                    .build();
        }

        velocityLibraryManager.loadLibrary(yaml);
        velocityLibraryManager.loadLibrary(updater);
        velocityLibraryManager.loadLibrary(discord);

        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");


        loadFiles();
        updateConfig();
        getLogger().info("§7Configurations loaded §dsuccessfully§7!");


        if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda = new JdaBuilder();
            jda.startJDA();
            updateJDATask();

            getLogger().info("§7Hooked into Discord §dsuccessfully§7!");

        }

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("screload")
                .aliases("staffchatreload", "staffreload", "cleanscreload", "cleanstaffchatreload")
                .build(), new ReloadCommand(this));

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("scdebug")
                .aliases("staffchatdebug", "staffdebug", "cleanscdebug", "cleanstaffchatdebug")
                .build(), new DebugCommand(this));

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

        if (VelocityRedis.REDIS_ENABLE.get(Boolean.class) && getRedisBungee()) {
            getLogger().error("RedisBungee was not found, the RedisBungee hook won't work.");
        }

        if (VelocityRedis.REDIS_ENABLE.get(Boolean.class) && getRedisBungee()) {
            registerRedisBungee();
            getLogger().info("§7Hooked into RedisBungee §dsuccessfully§7!");
        }

        if (VelocityConfig.STATS.get(Boolean.class)) {
            metricsFactory.make(this, 16447);
            getLogger().info("§7Metrics loaded §dsuccessfully§7!");
        }

        if (VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
            UpdateChecker();
        }

        if (!getUnsignedVelocityAddon()) {
            getLogger().warn("If you get kicked out in 1.19+ while typing in a staffchat on Velocity, " +
                    "consider downloading https://github.com/4drian3d/UnSignedVelocity/releases/latest");
        }

        getLogger().info("§7Plugin successfully §denabled§7!");

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) throws LoginException {
        getLogger().info("Deleting instances...");

        if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.getJda().shutdownNow();
        }

        instance = null;
        configTextFile = null;

        logger.info("§7Successfully §ddisabled§7.");
    }

    private void loadFiles() {

        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");
        discordTextFile = new TextFile(path, "discord.yml");
        aliasesTextFile = new TextFile(path, "aliases.yml");
        redisTextFile = new TextFile(path, "redis.yml");
        versionTextFile = new TextFile(path, "version.yml");

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

            logger.info("§7Creating new §dconfigurations§7...");
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

        final String[] aliases_stafflist = VelocityCommandsConfig.STAFFLIST.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.STAFFLIST.getStringList().get(0))
                .aliases(aliases_stafflist)
                .build(), new StaffListCommand(this));

    }

    @SneakyThrows
    private void registerStaffChat() {

        final String[] aliases_staffchat = VelocityCommandsConfig.STAFFCHAT.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.STAFFCHAT.getStringList().get(0))
                .aliases(aliases_staffchat)
                .build(), new StaffChatCommand(this));


        final String[] aliases_staffchatmute = VelocityCommandsConfig.STAFFCHAT_MUTE.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.STAFFCHAT_MUTE.getStringList().get(0))
                .aliases(aliases_staffchatmute)
                .build(), new MuteCommand(this));


        final String[] aliases_staffchattoggle = VelocityCommandsConfig.STAFFCHAT_TOGGLE.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.STAFFCHAT_TOGGLE.getStringList().get(0))
                .aliases(aliases_staffchattoggle)
                .build(), new ToggleCommand(this));


        final String[] aliases_staffchatafk = VelocityCommandsConfig.STAFFCHAT_AFK.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.STAFFCHAT_AFK.getStringList().get(0))
                .aliases(aliases_staffchatafk)
                .build(), new AFKCommand(this));

        server.getEventManager().register(this, new JoinListener(this));
        server.getEventManager().register(this, new ServerListener(this));
        server.getEventManager().register(this, new ChatListener(this));

        if (VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.staffchat.listeners.ChatListener(this));

        }
    }

    @SneakyThrows
    private void registerDonorChat() {

        final String[] aliases_donorchat = VelocityCommandsConfig.DONORCHAT.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.DONORCHAT.getStringList().get(0))
                .aliases(aliases_donorchat)
                .build(), new DonorChatCommand(this));

        final String[] aliases_donormute = VelocityCommandsConfig.DONORCHAT_MUTE.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.DONORCHAT_MUTE.getStringList().get(0))
                .aliases(aliases_donormute)
                .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.MuteCommand(this));

        final String[] aliases_donortoggle = VelocityCommandsConfig.DONORCHAT_TOGGLE.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.DONORCHAT_TOGGLE.getStringList().get(0))
                .aliases(aliases_donortoggle)
                .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.ToggleCommand(this));

        server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));

        if (VelocityConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));

        }
    }

    @SneakyThrows
    private void registerAdminChat() {

        final String[] aliases_adminchat = VelocityCommandsConfig.ADMINCHAT.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.ADMINCHAT.getStringList().get(0))
                .aliases(aliases_adminchat)
                .build(), new AdminChatCommand(this));

        final String[] aliases_adminchatmute = VelocityCommandsConfig.ADMINCHAT_MUTE.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.ADMINCHAT_MUTE.getStringList().get(0))
                .aliases(aliases_adminchatmute)
                .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.MuteCommand(this));

        final String[] aliases_adminchattoggle = VelocityCommandsConfig.ADMINCHAT_TOGGLE.getStringList().toArray(new String[0]);

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder(VelocityCommandsConfig.ADMINCHAT_TOGGLE.getStringList().get(0))
                .aliases(aliases_adminchattoggle)
                .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.ToggleCommand(this));

        server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));

        if (VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));

        }
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
            logger.error("Fatal error while updating JDA, please report this error on https://dsc.gg/futuredevelopment.");
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


    public boolean getRedisBungee() {
        return getServer().getPluginManager().isLoaded("redisbungee");
    }

    @SuppressWarnings("ALL")
    public boolean getUnsignedVelocityAddon() {
        return getServer().getPluginManager().isLoaded("unsignedvelocity");
    }
}