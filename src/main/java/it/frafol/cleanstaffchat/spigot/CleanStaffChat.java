package it.frafol.cleanstaffchat.spigot;

import it.frafol.cleanstaffchat.spigot.Commands.MuteCommand;
import it.frafol.cleanstaffchat.spigot.Commands.ReloadCommand;
import it.frafol.cleanstaffchat.spigot.Commands.StaffChatCommand;
import it.frafol.cleanstaffchat.spigot.Commands.ToggleCommand;
import it.frafol.cleanstaffchat.spigot.Listeners.ChatListener;
import it.frafol.cleanstaffchat.spigot.Listeners.JoinListener;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
import it.frafol.cleanstaffchat.spigot.objects.TextFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

public class CleanStaffChat extends JavaPlugin {

    private TextFile configTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    public void onEnable() {

        instance = this;

        getLogger().info("\n  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        getLogger().info("Registering commands...");
        getCommand("scmute").setExecutor(new MuteCommand(this));
        getCommand("sctoggle").setExecutor(new ToggleCommand(this));
        getCommand("screload").setExecutor(new ReloadCommand(this));
        getCommand("sc").setExecutor(new StaffChatCommand(this));
        getLogger().info("Commands registered successfully!");

        getLogger().info("Registering listeners...");
        getLogger().info("Server version: " + Bukkit.getServer().getBukkitVersion() + ".");
        if (Bukkit.getServer().getBukkitVersion().contains("1.19")) {
            getLogger().severe("Some of the functions will not be available for your version of the server.");
        }
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getLogger().info("Listeners registered successfully!");

        getLogger().info("Loading configurations...");
        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        getLogger().info("Configurations loaded successfully!");

        getLogger().info("Plugin successfully enabled, enjoy!");
    }

    public YamlFile getConfigTextFile() {
        return getInstance().configTextFile.getConfig();
    }

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