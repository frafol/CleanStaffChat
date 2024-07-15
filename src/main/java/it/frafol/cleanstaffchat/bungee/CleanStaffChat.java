package it.frafol.cleanstaffchat.bungee;

import com.alessiodp.libby.BungeeLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import it.frafol.cleanstaffchat.bungee.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.bungee.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.bungee.enums.*;
import it.frafol.cleanstaffchat.bungee.general.commands.MuteChatCommand;
import it.frafol.cleanstaffchat.bungee.hooks.RedisListener;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import it.frafol.cleanstaffchat.bungee.staffchat.commands.DebugCommand;
import it.frafol.cleanstaffchat.bungee.staffchat.commands.ReloadCommand;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.ServerListener;
import lombok.Getter;
import lombok.SneakyThrows;
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
import java.util.concurrent.TimeUnit;

@Getter
public class CleanStaffChat extends Plugin {

    private JDA jda;

    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile redisTextFile;
    private TextFile serversTextFile;
    private TextFile versionTextFile;

    public boolean updated = false;

    private final boolean getSpicord = getProxy().getPluginManager().getPlugin("Spicord") != null;

    @Getter
    public static CleanStaffChat instance;

    @SneakyThrows
    @Override
    public void onEnable() {

        instance = this;

        BungeeLibraryManager bungeeLibraryManager = new BungeeLibraryManager(this);

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
        final Relocation kotlin = new Relocation("kotlin", "it{}frafol{}libs{}kotlin");
        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("5.0.0-beta.18")
                .relocate(kotlin)
                .url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.18/JDA-5.0.0-beta.18-withDependencies-min.jar")
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
                    .relocate(yamlrelocation)
                    .build();
        }

        bungeeLibraryManager.loadLibrary(yaml);

        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        getLogger().info("Server version: " + getProxy().getVersion());
        checkIncompatibilities();

        loadFiles();
        updateConfig();
        getLogger().info("§7Configurations loaded §dsuccessfully§7!");

        if (BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
            UpdateChecker();
        }

        startJDA();

        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        getProxy().getPluginManager().registerCommand(this, new DebugCommand(this));
        getProxy().getPluginManager().registerListener(this, new DebugCommand(this));

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

        if (BungeeConfig.MUTECHAT_MODULE.get(Boolean.class)) {
            registerMuteChat();
        }

        if (BungeeRedis.REDIS_ENABLE.get(Boolean.class) && getProxy().getPluginManager().getPlugin("RedisBungee") == null) {
            getLogger().severe("RedisBungee was not found, the RedisBungee hook won't work.");
        }

        if (BungeeRedis.REDIS_ENABLE.get(Boolean.class) && getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            registerRedisBungee();
            getLogger().info("§7Hooked into RedisBungee §dsuccessfully§7!");
        }

        if (isPremiumVanish()) {
            getLogger().info("§7Hooked into PremiumVanish §dsuccessfully§7!");
        }

        if (BungeeConfig.STATS.get(Boolean.class)) {
            new Metrics(this, 16449);
            getLogger().info("§7Metrics loaded §asuccessfully§7!");
        }

        getProxy().registerChannel("cleansc:cancel");
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

    public YamlFile getServersTextFile() {
        return getInstance().serversTextFile.getConfig();
    }

    public YamlFile getVersionTextFile() {
        return getInstance().versionTextFile.getConfig();
    }

    private void checkIncompatibilities() {
        if (getSpicord) {
            getLogger().severe("Spicord found, this plugin is completely unsupported and you won't receive any support.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("§7Deleting §dinstances§7...");

        if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.shutdownNow();
        }

        instance = null;
        configTextFile = null;

        getProxy().unregisterChannel("cleansc:cancel");
        getLogger().info("§7Successfully §ddisabled§7.");
    }

    public void startJDA() {
        if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            try {
                jda = JDABuilder.createDefault(BungeeDiscordConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            } catch (ExceptionInInitializerError e) {
                getLogger().severe("Invalid Discord configuration, please check your discord.yml file.");
                getLogger().severe("Make sure you are not using any strange forks (like Aegis).");
            }

            updateJDATask();
            getLogger().info("§7Hooked into Discord §dsuccessfully§7!");
        }
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
                    player.sendMessage(TextComponent.fromLegacy(BungeeMessages.UPDATE.color()
                            .replace("%version%", version)
                            .replace("%prefix%", BungeeMessages.PREFIX.color())));
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
            YamlUpdater.create(new File(getDataFolder().toPath() + "/servers.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/main/src/main/resources/servers.yml"))
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

    private void registerMuteChat() {
        getProxy().getPluginManager().registerCommand(this, new MuteChatCommand());
        getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.general.listeners.ChatListener(this));
    }

    private void registerStaffList() {

        if (getProxy().getPluginManager().getPlugin("LuckPerms") == null && getProxy().getPluginManager().getPlugin("UltraPermissions") == null) {
            getLogger().warning("You need LuckPerms or UltraPermissions to use StaffList.");
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
        serversTextFile = new TextFile(getDataFolder().toPath(), "servers.yml");
        versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");

    }

    private void updateJDATask() {
        getProxy().getScheduler().schedule(this, this::updateJDA, 1L, 30L, TimeUnit.SECONDS);
    }

    public void updateJDA() {

        if (!BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            return;
        }

        if (jda == null) {
            getLogger().severe("Fatal error while updating JDA. Please report this error to https://dsc.gg/futuredevelopment.");
            return;
        }

        if (isPremiumVanish()) {
            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                            (BungeeDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                    BungeeDiscordConfig.DISCORD_ACTIVITY.get(String.class)
                            .replace("%players%", String.valueOf(getProxy().getOnlineCount() - BungeeVanishAPI.getInvisiblePlayers().size()))));
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

        } catch (IOException ignored) {
            getLogger().severe("Error while updating CleanStaffChat, please report this error on https://dsc.gg/futuredevelopment.");
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
        if (BungeeConfig.PREMIUMVANISH.get(Boolean.class)) {
            return getProxy().getPluginManager().getPlugin("PremiumVanish") != null;
        }
        return false;
    }

    public boolean isInBlockedStaffChatServer(ProxiedPlayer player) {
        return (!BungeeServers.SC_BLOCKED_SRV.getStringList().isEmpty() && BungeeServers.SC_BLOCKED_SRV.getStringList().contains(player.getServer().getInfo().getName()));
    }

    public boolean isInBlockedAdminChatServer(ProxiedPlayer player) {
        return (!BungeeServers.AC_BLOCKED_SRV.getStringList().isEmpty() && BungeeServers.AC_BLOCKED_SRV.getStringList().contains(player.getServer().getInfo().getName()));
    }

    public boolean isInBlockedDonorChatServer(ProxiedPlayer player) {
        return (!BungeeServers.DC_BLOCKED_SRV.getStringList().isEmpty() && BungeeServers.DC_BLOCKED_SRV.getStringList().contains(player.getServer().getInfo().getName()));
    }
}