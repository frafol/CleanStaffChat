package it.frafol.cleanstaffchat.bukkit;

import it.frafol.cleanstaffchat.bukkit.enums.SpigotCommandsConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.objects.TextFile;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.ReloadCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.MoveListener;
import lombok.SneakyThrows;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CleanStaffChat extends JavaPlugin {

    private JDA jda;
    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @SneakyThrows
    @Override
    public void onEnable() {

        instance = this;

        BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);

        Library yaml = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.3")
                .build();

        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("5.0.0-beta.5")
                .url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.5/JDA-5.0.0-beta.5-withDependencies-min.jar")
                .build();

        bukkitLibraryManager.addMavenCentral();
        bukkitLibraryManager.addJitPack();
        bukkitLibraryManager.loadLibrary(discord);
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

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");
        discordTextFile = new TextFile(getDataFolder().toPath(), "discord.yml");
        aliasesTextFile = new TextFile(getDataFolder().toPath(), "aliases.yml");
        getLogger().info("Configurations loaded successfully!");

        getCommandMap().register(getName().toLowerCase(), new ReloadCommand(this));

        if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda = JDABuilder.createDefault(SpigotDiscordConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            updateJDA();

            getLogger().info("Hooked into Discord successfully!");

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

        if (SpigotConfig.STATS.get(Boolean.class) && !getDescription().getVersion().contains("alpha")) {

            new Metrics(this, 16448);

            getLogger().info("Metrics loaded successfully!");
        }

        if (SpigotConfig.UPDATE_CHECK.get(Boolean.class) && !getDescription().getVersion().contains("alpha")) {
            new UpdateCheck(this).getVersion(version -> {
                if (!this.getDescription().getVersion().equals(version)) {
                    getLogger().warning("There is a new update available, download it on https://bit.ly/3BOQFEz");
                }
            });
        }

        getLogger().info("Plugin successfully enabled!");

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

    public JDA getJda() {
        return jda;
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
            if (!getDescription().getVersion().equals(version)) {
                player.sendMessage(ChatColor.YELLOW + "[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz");
            }
        });
    }

    public void updateJDA() {

        if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            return;
        }

        if (jda == null) {
            getLogger().severe("Fatal error while updating JDA. Please report this error to discord.io/futurevelopment.");
            return;
        }

        jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                        (SpigotDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                SpigotDiscordConfig.DISCORD_ACTIVITY.get(String.class)
                        .replace("%players%", String.valueOf(getServer().getOnlinePlayers().size()))));

    }

}