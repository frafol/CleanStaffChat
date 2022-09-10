package it.frafol.cleanstaffchat.bungee;

import it.frafol.cleanstaffchat.bungee.Commands.MuteCommand;
import it.frafol.cleanstaffchat.bungee.Commands.ReloadCommand;
import it.frafol.cleanstaffchat.bungee.Commands.StaffChatCommand;
import it.frafol.cleanstaffchat.bungee.Commands.ToggleCommand;
import it.frafol.cleanstaffchat.bungee.Listeners.ChatListener;
import it.frafol.cleanstaffchat.bungee.Listeners.JoinListener;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;

public class CleanStaffChat extends Plugin {

    private TextFile configTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    public void onEnable() {

        instance = this;

        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        getLogger().info("§7Registering commands...");
        getProxy().getPluginManager().registerCommand(this, new StaffChatCommand());
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        getProxy().getPluginManager().registerCommand(this, new MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new ToggleCommand());
        getLogger().info("§7Commands registered §asuccessfully§7!");

        getLogger().info("§7Registering listeners...");
        getProxy().getPluginManager().registerListener(this, new JoinListener(this));
        getProxy().getPluginManager().registerListener(this, new ChatListener(this));
        getLogger().info("§7Listeners registered §asuccessfully§7!");

        getLogger().info("§7Loading configurations...");
        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        getLogger().info("§7Configurations loaded §asuccessfully§7!");

        getLogger().info("§7Plugin successfully §aenabled§7, enjoy!");
    }

    public YamlFile getConfigTextFile() {
        return getInstance().configTextFile.getConfig();
    }

    public void onDisable() {
        getLogger().info("§7Deleting instances...");
        instance = null;
        configTextFile = null;

        getLogger().info("§7Clearing lists...");
        PlayerCache.getToggled_2().clear();
        PlayerCache.getToggled().clear();
        PlayerCache.getMuted().clear();

        getLogger().info("§7Successfully §cdisabled§7.");
    }
}