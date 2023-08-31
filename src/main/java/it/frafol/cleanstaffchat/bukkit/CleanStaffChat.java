package it.frafol.cleanstaffchat.bukkit;

import com.tchristofferson.configupdater.ConfigUpdater;
import it.frafol.cleanstaffchat.bukkit.enums.*;
import it.frafol.cleanstaffchat.bukkit.objects.TextFile;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.DebugCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.ReloadCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.MoveListener;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class CleanStaffChat extends JavaPlugin {

    private JDA jda;

    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile versionTextFile;

    @Getter
    private static CleanStaffChat instance;

    public boolean updated = false;

    @SneakyThrows
    @Override
    public void onEnable() {

        instance = this;

        BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);

        Library yaml;
        yaml = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.4")
                .build();

        final Relocation kotlin = new Relocation("kotlin", "it{}frafol{}libs{}kotlin");
        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("5.0.0-beta.12")
                .relocate(kotlin)
                .url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.12/JDA-5.0.0-beta.12-withDependencies-min.jar")
                .build();

        bukkitLibraryManager.addMavenCentral();
        bukkitLibraryManager.addJitPack();
        bukkitLibraryManager.loadLibrary(discord);

        try {
            bukkitLibraryManager.loadLibrary(yaml);
        } catch (RuntimeException ignored) {
            getLogger().severe("Failed to load Simple-YAML, trying to download it from GitHub...");
            yaml = Library.builder()
                    .groupId("me{}carleslc{}Simple-YAML")
                    .artifactId("Simple-Yaml")
                    .version("1.8.4")
                    .url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
                    .build();
        }

        bukkitLibraryManager.loadLibrary(yaml);

        getLogger().info("\n  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");


        getLogger().info("Server version: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".");

        if (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_6_R")
            || Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_5_R")
                || Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_4_R")
                || Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_3_R")
                || Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_2_R")
                || Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_1_R")
                || Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains("1_0_R")) {

            getLogger().severe("Support for your version was declined.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        File configFile = new File(getDataFolder(), "config.yml");
        File messageFile = new File(getDataFolder(), "messages.yml");
        File discordFile = new File(getDataFolder(), "discord.yml");
        File aliasesFile = new File(getDataFolder(), "aliases.yml");
        versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");

        if (!getDescription().getVersion().equals(SpigotVersion.VERSION.get(String.class))) {

            getLogger().info("Creating new configurations...");
            try {
                ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
                ConfigUpdater.update(this, "messages.yml", messageFile, Collections.emptyList());
                ConfigUpdater.update(this, "discord.yml", discordFile, Collections.emptyList());
                ConfigUpdater.update(this, "aliases.yml", aliasesFile, Collections.emptyList());
            } catch (IOException ignored) {
                getLogger().severe("Unable to update the files. Are you on Windows?");
            }

            versionTextFile.getConfig().set("version", getDescription().getVersion());
            versionTextFile.getConfig().save();

        }

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");
        discordTextFile = new TextFile(getDataFolder().toPath(), "discord.yml");
        aliasesTextFile = new TextFile(getDataFolder().toPath(), "aliases.yml");
        getLogger().info("Configurations loaded successfully!");

        getCommandMap().register(getName().toLowerCase(), new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new DebugCommand(), this);

        if (SpigotConfig.STAFFLIST_MODULE.get(Boolean.class)) {
            registerStaffListCommands();
        }

        if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            try {
                jda = JDABuilder.createDefault(SpigotDiscordConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            } catch (ExceptionInInitializerError e) {
                getLogger().severe("§cInvalid Discord configuration, please check your discord.yml file.");
            }
            updateJDATask();

            getLogger().info("Hooked into Discord successfully!");

        }

        if (isPremiumVanish()) {
            getLogger().info("Hooked into PremiumVanish successfully!");
        }

        if (isSuperVanish()) {
            getLogger().info("Hooked into SuperVanish successfully!");
        }

        if (SpigotConfig.STAFFCHAT.get(Boolean.class)) {
            registerStaffChatCommands();
            getServer().getPluginManager().registerEvents(new JoinListener(this), this);
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
            getServer().getPluginManager().registerEvents(new MoveListener(this), this);

            if (SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class) && SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new ChatListener(this));
            }
        }

        if (SpigotConfig.DONORCHAT.get(Boolean.class)) {
            registerDonorChatCommands();
            getServer().getPluginManager().registerEvents(new it.frafol.cleanstaffchat.bukkit.donorchat.listeners.ChatListener(this), this);

            if (SpigotConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.bukkit.donorchat.listeners.ChatListener(this));
            }
        }

        if (SpigotConfig.ADMINCHAT.get(Boolean.class)) {
            registerAdminChatCommands();
            getServer().getPluginManager().registerEvents(new it.frafol.cleanstaffchat.bukkit.adminchat.listeners.ChatListener(this), this);

            if (SpigotConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.bukkit.adminchat.listeners.ChatListener(this));
            }

        }

        if (SpigotConfig.MUTECHAT_MODULE.get(Boolean.class)) {
            registerMuteChatCommands();
            getServer().getPluginManager().registerEvents(new it.frafol.cleanstaffchat.bukkit.general.listeners.ChatListener(this), this);
        }

        if (SpigotConfig.STATS.get(Boolean.class) && !getDescription().getVersion().contains("alpha")) {
            new Metrics(this, 16448);
            getLogger().info("Metrics loaded successfully!");
        }

        UpdateChecker();
        getLogger().info("Plugin successfully enabled!");

    }

    private void UpdateChecker() {

        if (!SpigotConfig.UPDATE_CHECK.get(Boolean.class)) {
            return;
        }

        new UpdateCheck(this).getVersion(version -> {

            if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {

                if (SpigotConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
                    autoUpdate();
                    return;
                }

                if (!updated) {
                    getLogger().warning("There is a new update available, download it on SpigotMC!");
                }
            }

            if (Integer.parseInt(getDescription().getVersion().replace(".", "")) > Integer.parseInt(version.replace(".", ""))) {
                getLogger().warning("You are using a development version, please report any bugs!");
            }

        });
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
            getLogger().severe("Unable to update the plugin, please download the latest version manually. Are you on Windows?");
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

    @SneakyThrows
    private void registerStaffChatCommands() {
        List<Command> staffChatCommands = new ArrayList<>();
        for(SpigotCommandsConfig commandConfig : SpigotCommandsConfig.getStaffChatCommands()){
            List<String> commandLabels = commandConfig.getStringList();
            if (commandLabels.isEmpty()) {
                continue;
            }

            staffChatCommands.add((CommandBase) commandConfig.getCommandClass().getDeclaredConstructors()[0].newInstance(
                    this,
                    commandLabels.get(0),
                    "",
                    commandLabels.subList(1, commandLabels.size())
            ));
        }
        getCommandMap().registerAll(getName().toLowerCase(), staffChatCommands);
    }

    @SneakyThrows
    private void registerStaffListCommands() {
        
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().warning("You need LuckPerms to use the StaffList.");
            return;
        }

        List<Command> staffListCommands = new ArrayList<>();
        for(SpigotCommandsConfig commandsList : SpigotCommandsConfig.getStaffListCommands()){
            List<String> commandLabels = commandsList.getStringList();
            if (commandLabels.isEmpty()) {
                continue;
            }

            staffListCommands.add((CommandBase) commandsList.getCommandClass().getDeclaredConstructors()[0].newInstance(
                    this,
                    commandLabels.get(0),
                    "",
                    commandLabels.subList(1, commandLabels.size())
            ));
        }
        getCommandMap().registerAll(getName().toLowerCase(), staffListCommands);
    }

    @SneakyThrows
    private void registerAdminChatCommands() {
        List<Command> adminChatCommands = new ArrayList<>();
        for(SpigotCommandsConfig commandConfig : SpigotCommandsConfig.getAdminChatCommands()){
            List<String> commandLabels = commandConfig.getStringList();
            if (commandLabels.isEmpty()) {
                continue;
            }

            adminChatCommands.add((CommandBase) commandConfig.getCommandClass().getDeclaredConstructors()[0].newInstance(
                    this,
                    commandLabels.get(0),
                    "",
                    commandLabels.subList(1, commandLabels.size())
            ));
        }
        getCommandMap().registerAll(getName().toLowerCase(), adminChatCommands);
    }

    @SneakyThrows
    private void registerMuteChatCommands() {
        List<Command> muteChatCommands = new ArrayList<>();
        for(SpigotCommandsConfig commandConfig : SpigotCommandsConfig.getMuteChatCommands()){
            List<String> commandLabels = commandConfig.getStringList();
            if (commandLabels.isEmpty()) {
                continue;
            }

            muteChatCommands.add((CommandBase) commandConfig.getCommandClass().getDeclaredConstructors()[0].newInstance(
                    this,
                    commandLabels.get(0),
                    "",
                    commandLabels.subList(1, commandLabels.size())
            ));
        }
        getCommandMap().registerAll(getName().toLowerCase(), muteChatCommands);
    }

    @SneakyThrows
    private void registerDonorChatCommands() {
        List<Command> donorChatCommands = new ArrayList<>();
        for(SpigotCommandsConfig commandConfig : SpigotCommandsConfig.getDonorChatCommands()){
            List<String> commandLabels = commandConfig.getStringList();
            if (commandLabels.isEmpty()) {
                continue;
            }

            donorChatCommands.add((CommandBase) commandConfig.getCommandClass().getDeclaredConstructors()[0].newInstance(
                    this,
                    commandLabels.get(0),
                    "",
                    commandLabels.subList(1, commandLabels.size())
            ));
        }
        getCommandMap().registerAll(getName().toLowerCase(), donorChatCommands);
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        Field commandMap = getServer().getClass().getDeclaredField("commandMap");
        commandMap.setAccessible(true);
        return (CommandMap) commandMap.get(getServer());
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

    public YamlFile getVersionTextFile() {
        return getInstance().versionTextFile.getConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Deleting instances...");

        if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.shutdownNow();
        }

        instance = null;
        configTextFile = null;

        getLogger().info("Successfully disabled.");
    }

    public void UpdateCheck(Player player) {

        new UpdateCheck(this).getVersion(version -> {
            if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {

                if (SpigotConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
                    autoUpdate();
                    return;
                }

                if (!updated) {
                    player.sendMessage(SpigotMessages.UPDATE.color()
                            .replace("%version%", version)
                            .replace("%prefix%", SpigotMessages.PREFIX.color()));
                }
            }
        });
    }

    private void updateJDATask() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::updateJDA, 1, 20 * 30);
    }

    public void updateJDA() {

        if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            return;
        }

        if (jda == null) {
            getLogger().severe("Fatal error while updating JDA. Please report this error to https://dsc.gg/futuredevelopment.");
            return;
        }

        jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                        (SpigotDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                SpigotDiscordConfig.DISCORD_ACTIVITY.get(String.class)
                        .replace("%players%", String.valueOf(getServer().getOnlinePlayers().size()))));

    }

    public boolean isPremiumVanish() {
        if (SpigotConfig.PREMIUMVANISH.get(Boolean.class)) {
            return getServer().getPluginManager().getPlugin("PremiumVanish") != null;
        }
        return false;
    }

    public boolean isSuperVanish() {
        if (SpigotConfig.SUPERVANISH.get(Boolean.class)) {
            return getServer().getPluginManager().getPlugin("SuperVanish") != null;
        }
        return false;
    }
}