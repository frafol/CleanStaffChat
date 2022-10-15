package it.frafol.cleanstaffchat.spigot;

import it.frafol.cleanstaffchat.spigot.Commands.MuteCommand;
import it.frafol.cleanstaffchat.spigot.Commands.ReloadCommand;
import it.frafol.cleanstaffchat.spigot.Commands.StaffChatCommand;
import it.frafol.cleanstaffchat.spigot.Commands.ToggleCommand;
import it.frafol.cleanstaffchat.spigot.Listeners.ChatListener;
import it.frafol.cleanstaffchat.spigot.Listeners.JoinListener;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
import it.frafol.cleanstaffchat.spigot.objects.TextFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Objects;

public class CleanStaffChat extends JavaPlugin {

    private TextFile configTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;

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


        Objects.requireNonNull(getCommand("scmute")).setExecutor(new MuteCommand(this));
        Objects.requireNonNull(getCommand("sctoggle")).setExecutor(new ToggleCommand(this));
        Objects.requireNonNull(getCommand("screload")).setExecutor(new ReloadCommand(this));
        Objects.requireNonNull(getCommand("sc")).setExecutor(new StaffChatCommand(this));
        getLogger().info("Commands registered successfully!");

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getLogger().info("Listeners registered successfully!");

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        getLogger().info("Configurations loaded successfully!");

        if (SpigotConfig.STATS.get(Boolean.class)) {

            new Metrics(this, 16448);

            getLogger().info("Metrics loaded successfully!");
        }

        if (SpigotConfig.UPDATE_CHECK.get(Boolean.class)) {
            new UpdateCheck(this).getVersion(version -> {
                if (!this.getDescription().getVersion().equals(version)) {
                    getLogger().warning("There is a new update available, download it on SpigotMC!");
                }
            });
        }

        getLogger().info("Plugin successfully enabled!");
    }

    public YamlFile getConfigTextFile() {
        return getInstance().configTextFile.getConfig();
    }


    @Override
    public void onDisable() {
        getLogger().info("Deleting instances...");
        instance = null;
        configTextFile = null;

        getLogger().info("Clearing lists...");
        PlayerCache.getToggled_2().clear();
        PlayerCache.getToggled().clear();
        PlayerCache.getMuted().clear();

        getLogger().info("Successfully disabled.");
    }
}