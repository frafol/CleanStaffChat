package it.frafol.cleanstaffchat.bungee;

import it.frafol.cleanstaffchat.bungee.Commands.MuteCommand;
import it.frafol.cleanstaffchat.bungee.Commands.ReloadCommand;
import it.frafol.cleanstaffchat.bungee.Commands.StaffChatCommand;
import it.frafol.cleanstaffchat.bungee.Commands.ToggleCommand;
import it.frafol.cleanstaffchat.bungee.Listeners.ChatListener;
import it.frafol.cleanstaffchat.bungee.Listeners.JoinListener;
import it.frafol.cleanstaffchat.bungee.Listeners.ServerListener;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Timer;
import java.util.TimerTask;

public class CleanStaffChat extends Plugin {

    private TextFile configTextFile;
    public static CleanStaffChat instance;
    Timer timer = new Timer ();

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;

        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        getProxy().getPluginManager().registerCommand(this, new StaffChatCommand());
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
        getProxy().getPluginManager().registerCommand(this, new MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new ToggleCommand());
        getLogger().info("§7Commands registered §asuccessfully§7!");

        getProxy().getPluginManager().registerListener(this, new JoinListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerListener(this));
        getProxy().getPluginManager().registerListener(this, new ChatListener(this));
        getLogger().info("§7Listeners registered §asuccessfully§7!");

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        getLogger().info("§7Configurations loaded §asuccessfully§7!");


        if (BungeeConfig.STATS.get(Boolean.class)) {

            new Metrics(this, 16449);

            getLogger().info("§7Metrics loaded §asuccessfully§7!");
        }


        if (BungeeConfig.UPDATE_CHECK.get(Boolean.class)) {
            new UpdateCheck(this).getVersion(version -> {
                if (!this.getDescription().getVersion().equals(version)) {
                    getLogger().warning("§eThere is a new update available, download it on SpigotMC!");
                }
            });

        }

        getLogger().info("§7Plugin successfully §aenabled§7!");
    }

    public YamlFile getConfigTextFile() {
        return getInstance().configTextFile.getConfig();
    }

    @Override
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