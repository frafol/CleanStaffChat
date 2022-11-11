package it.frafol.cleanstaffchat.bungee;

import it.frafol.cleanstaffchat.bungee.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.bungee.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import it.frafol.cleanstaffchat.bungee.staffchat.commands.ReloadCommand;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bungee.staffchat.listeners.ServerListener;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;

public class CleanStaffChat extends Plugin {

    private JDA jda;
    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    public static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @SneakyThrows
    @Override
    public void onEnable() {

        instance = this;

        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");
        discordTextFile = new TextFile(getDataFolder().toPath(), "discord.yml");
        aliasesTextFile = new TextFile(getDataFolder().toPath(), "aliases.yml");
        getLogger().info("§7Configurations loaded §dsuccessfully§7!");

        if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda = JDABuilder.createDefault(BungeeDiscordConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                            (BungeeDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                    BungeeDiscordConfig.DISCORD_ACTIVITY.get(String.class)));

            if (getProxy().getPluginManager().getPlugin("ServerUtils") != null
                    || getProxy().getPluginManager().getPlugin("PlugManBungee") != null) {

                if (getProxy().getPluginManager().getPlugin("ServerUtils") != null ) {

                    getLogger().warning("\n§f\n§e§lWARNING!" +
                            "\n§f\n§7Integration on Discord may give you many problems if you reload the plugin with ServerUtils." +
                            "\n§7Consider performing a §d§lTOTAL RESTART to prevent issues!\n");

                } else {

                    getLogger().warning("\n§f\n§e§lWARNING!" +
                            "\n§f\n§7Integration on Discord may give you many problems if you reload the plugin with PlugManBungee." +
                            "\n§7Consider performing a §d§lTOTAL RESTART to prevent issues!\n");

                }

            }

            getLogger().info("§7Hooked into Discord §dsuccessfully§7!");

        }

        if (BungeeConfig.STAFFCHAT.get(Boolean.class)) {

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

        if (BungeeConfig.ADMINCHAT.get(Boolean.class)) {

            getProxy().getPluginManager().registerCommand(this, new AdminChatCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.adminchat.commands.MuteCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.adminchat.commands.ToggleCommand());
            getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.adminchat.listeners.ChatListener(this));

            if (BungeeConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.bungee.adminchat.listeners.ChatListener(this));
            }

        }

        if (BungeeConfig.DONORCHAT.get(Boolean.class)) {

            getProxy().getPluginManager().registerCommand(this, new DonorChatCommand(this));
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.donorchat.commands.MuteCommand());
            getProxy().getPluginManager().registerCommand(this, new it.frafol.cleanstaffchat.bungee.donorchat.commands.ToggleCommand());
            getProxy().getPluginManager().registerListener(this, new it.frafol.cleanstaffchat.bungee.donorchat.listeners.ChatListener(this));

            if (BungeeConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.bungee.donorchat.listeners.ChatListener(this));
            }

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

    public JDA getJda() {
        return jda;
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
        PlayerCache.getCooldown().clear();
        PlayerCache.getCooldown_discord().clear();
        PlayerCache.getToggled_admin().clear();
        PlayerCache.getToggled_donor().clear();
        PlayerCache.getMuted().clear();
        PlayerCache.getMuted_admin().clear();
        PlayerCache.getMuted_donor().clear();
        PlayerCache.getAfk().clear();

        getLogger().info("§7Successfully §ddisabled§7.");
    }

}