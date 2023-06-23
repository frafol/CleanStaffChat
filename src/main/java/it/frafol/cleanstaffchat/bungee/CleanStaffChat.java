package it.frafol.cleanstaffchat.bungee;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.bungee.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeRedis;
import it.frafol.cleanstaffchat.bungee.enums.BungeeVersion;
import it.frafol.cleanstaffchat.bungee.hooks.RedisListener;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import it.frafol.cleanstaffchat.bungee.staffchat.commands.DebugCommand;
import it.frafol.cleanstaffchat.bungee.staffchat.commands.ReloadCommand;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.ServerListener;
import lombok.SneakyThrows;
import net.byteflux.libby.BungeeLibraryManager;
import net.byteflux.libby.Library;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CleanStaffChat extends Plugin {

    private JDA jda;
    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile redisTextFile;
    private TextFile versionTextFile;

    public boolean updated = false;

    public static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @SneakyThrows
    @Override
    public void onEnable() {

        instance = this;

        BungeeLibraryManager bungeeLibraryManager = new BungeeLibraryManager(this);

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

        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("5.0.0-beta.10")
                .url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.10/JDA-5.0.0-beta.10-withDependencies-min.jar")
                .build();

        bungeeLibraryManager.addMavenCentral();
        bungeeLibraryManager.addJitPack();
        bungeeLibraryManager.loadLibrary(updater);
        bungeeLibraryManager.loadLibrary(discord);

        try {
            bungeeLibraryManager.loadLibrary(yaml);
        } catch (RuntimeException ignored) {
            getLogger().severe("Failed to load Simple-YAML, trying to download it from GitHub...");
            yaml = Library.builder()
                    .groupId("me{}carleslc{}Simple-YAML")
                    .artifactId("Simple-Yaml")
                    .version("1.8.4")
                    .url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
                    .build();
        }

        bungeeLibraryManager.loadLibrary(yaml);

        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        loadFiles();
        updateConfig();
        getLogger().info("§7Configurations loaded §dsuccessfully§7!");

        if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda = JDABuilder.createDefault(BungeeDiscordConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            updateJDA();

            getLogger().info("§7Hooked into Discord §dsuccessfully§7!");

        }

        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        getProxy().getPluginManager().registerCommand(this, new DebugCommand(this));

        if (BungeeConfig.STAFFLIST_MODULE.get(Boolean.class)) {

            registerStaffList();

        }

        if (BungeeConfig.STAFFCHAT.get(Boolean.class)) {

            registerStaffChat();

        }

        if (BungeeConfig.ADMINCHAT.get(Boolean.class)) {

            registerAdminChat();

        }

        if (BungeeConfig.DONORCHAT.get(Boolean.class)) {

            registerDonorChat();

        }

        if (BungeeRedis.REDIS_ENABLE.get(Boolean.class) && getProxy().getPluginManager().getPlugin("RedisBungee") == null) {

            getLogger().severe("RedisBungee was not found, the RedisBungee hook won't work.");

        }

        if (BungeeRedis.REDIS_ENABLE.get(Boolean.class) && getProxy().getPluginManager().getPlugin("RedisBungee") != null) {

            registerRedisBungee();

            getLogger().info("§7Hooked into RedisBungee §dsuccessfully§7!");

        }

        if (BungeeConfig.STATS.get(Boolean.class)) {

            new Metrics(this, 16449);

            getLogger().info("§7Metrics loaded §asuccessfully§7!");
        }

        if (BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {

            UpdateChecker();

        }

        getLogger().info("§7Plugin successfully §denabled§7!");
    }

    public YamlFile getConfigTextFile() {
        return getInstance().configTextFile.getConfig();
    }

    public YamlFile getMessagesTextFile() {
        return getInstance().messagesTextFile.getConfig();
    }

    public YamlFile getDiscordTextFile() {
        return getInstance().discordTextFile.getConfig();
    }

    public YamlFile getAliasesTextFile() {
        return getInstance().aliasesTextFile.getConfig();
    }

    public YamlFile getRedisTextFile() {
        return getInstance().redisTextFile.getConfig();
    }

    public YamlFile getVersionTextFile() {
        return getInstance().versionTextFile.getConfig();
    }

    public JDA getJda() {
        return jda;
    }

    @Override
    public void onDisable() {
        getLogger().info("§7Deleting §dinstances§7...");

        if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.shutdownNow();
        }

        instance = null;
        configTextFile = null;

        getLogger().info("§7Successfully §ddisabled§7.");
    }

    private void UpdateChecker() {
        if (!BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
            return;
        }

        new UpdateCheck(this).getVersion(version -> {

            if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {

                if (BungeeConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
                    autoUpdate();
                    return;
                }

                if (!updated) {
                    getLogger().warning("§eThere is a new update available, download it on SpigotMC!");
                }
            }

            if (Integer.parseInt(getDescription().getVersion().replace(".", "")) > Integer.parseInt(version.replace(".", ""))) {
                getLogger().warning("§eYou are using a development version, please report any bugs!");
            }

        });
    }

    public void UpdateCheck(ProxiedPlayer player) {

        if (!BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
            return;
        }

        new UpdateCheck(this).getVersion(version -> {

            if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {

                if (BungeeConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
                    autoUpdate();
                    return;
                }

                if (!updated) {
                    player.sendMessage(TextComponent.fromLegacyText("§e[CleanStaffChat] There is a new update available, download it on SpigotMC!"));
                }
            }
        });
    }

    @SneakyThrows
    private void updateConfig() {
        if (!getDescription().getVersion().equals(BungeeVersion.VERSION.get(String.class))) {

            getLogger().info("§7Creating new §dconfigurations§7...");
            YamlUpdater.create(new File(getDataFolder().toPath() + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/config.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(getDataFolder().toPath() + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/messages.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(getDataFolder().toPath() + "/discord.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/discord.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(getDataFolder().toPath() + "/redis.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/redis.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(getDataFolder().toPath() + "/aliases.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/aliases.yml"))
                    .backup(true)
                    .update();
            versionTextFile.getConfig().set("version", getDescription().getVersion());
            versionTextFile.getConfig().save();
            loadFiles();
        }
    }

    private void registerRedisBungee() {

        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

        getProxy().getPluginManager().registerListener(this, new RedisListener(this));

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

    private void registerDonorChat() {

        getProxy().getPluginManager().registerCommand(this, new DonorChatCommand(this));
        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.donorchat.commands.MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.donorchat.commands.ToggleCommand());
        getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.donorchat.listeners.ChatListener(this));

        if (BungeeConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.addEventListener(new it.frafol.cleanstaffchat.bungee.donorchat.listeners.ChatListener(this));
        }

    }

    private void registerAdminChat() {

        getProxy().getPluginManager().registerCommand(this, new AdminChatCommand());
        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.adminchat.commands.MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.adminchat.commands.ToggleCommand());
        getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.adminchat.listeners.ChatListener(this));

        if (BungeeConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.addEventListener(new it.frafol.cleanstaffchat.bungee.adminchat.listeners.ChatListener(this));
        }

    }

    private void registerStaffList() {

        if (getProxy().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().warning("You need LuckPermsBungee to use StaffList.");
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.StaffListCommand());
    }

    private void registerStaffChat() {

        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.StaffChatCommand());
        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.ToggleCommand());
        getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.AFKCommand());
        getProxy().getPluginManager().registerListener(this, new JoinListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerListener(this));
        getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.staffchat.listeners.ChatListener(this));

        if (BungeeConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class) && BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.addEventListener(new ChatListener(this));
        }

    }

    private void loadFiles() {

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");
        discordTextFile = new TextFile(getDataFolder().toPath(), "discord.yml");
        aliasesTextFile = new TextFile(getDataFolder().toPath(), "aliases.yml");
        redisTextFile = new TextFile(getDataFolder().toPath(), "redis.yml");
        versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");

    }

    public void updateJDA() {

        if (!BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            return;
        }

        if (jda == null) {
            getLogger().severe("Fatal error while updating JDA. Please report this error to discord.io/futurevelopment.");
            return;
        }

        jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                        (BungeeDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                BungeeDiscordConfig.DISCORD_ACTIVITY.get(String.class)
                        .replace("%players%", String.valueOf(getProxy().getOnlineCount()))));

    }

    public void autoUpdate() {
        try {
            String fileUrl = "https://github.com/frafol/CleanStaffChat/releases/download/release/CleanStaffChat.jar";
            String destination = "./plugins/";

            String fileName = getFileNameFromUrl(fileUrl);
            File outputFile = new File(destination, fileName);

            downloadFile(fileUrl, outputFile);
            updated = true;
            getLogger().warning("CleanStaffChat successfully updated, a restart is required.");

        } catch (IOException e) {
            e.printStackTrace();
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

}