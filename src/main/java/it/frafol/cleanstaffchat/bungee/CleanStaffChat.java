package it.frafol.cleanstaffchat.bungee;

import it.frafol.cleanstaffchat.bungee.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.bungee.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import it.frafol.cleanstaffchat.bungee.staffchat.commands.ReloadCommand;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.ServerListener;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;

public class CleanStaffChat extends Plugin {

    private TextFile configTextFile;
    public static CleanStaffChat instance;

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

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        getLogger().info("§7Configurations loaded §asuccessfully§7!");

        if (BungeeConfig.STAFFCHAT.get(Boolean.class)) {

            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.StaffChatCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.MuteCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.ToggleCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.staffchat.commands.AFKCommand());
            getProxy().getPluginManager().registerListener(this, new JoinListener(this));
            getProxy().getPluginManager().registerListener(this, new ServerListener(this));
            getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.staffchat.listeners.ChatListener(this));

        }

        if (BungeeConfig.ADMINCHAT.get(Boolean.class)) {

            getProxy().getPluginManager().registerCommand(this, new AdminChatCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.adminchat.commands.MuteCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.adminchat.commands.ToggleCommand());
            getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.adminchat.listeners.ChatListener(this));

        }

        if (BungeeConfig.DONORCHAT.get(Boolean.class)) {

            getProxy().getPluginManager().registerCommand(this, new DonorChatCommand(this));
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.donorchat.commands.MuteCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.donorchat.commands.ToggleCommand());
            getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.donorchat.listeners.ChatListener(this));

        }

        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());


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
        PlayerCache.getToggled_2_admin().clear();
        PlayerCache.getToggled_2_donor().clear();
        PlayerCache.getToggled().clear();
        PlayerCache.getToggled_admin().clear();
        PlayerCache.getToggled_donor().clear();
        PlayerCache.getMuted().clear();
        PlayerCache.getMuted_admin().clear();
        PlayerCache.getMuted_donor().clear();
        PlayerCache.getAfk().clear();

        getLogger().info("§7Successfully §cdisabled§7.");
    }
}