package it.frafol.cleanstaffchat.bukkit;

import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.objects.TextFile;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.*;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.MoveListener;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Objects;

public class CleanStaffChat extends JavaPlugin {

    private JDA jda;
    private TextFile configTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @SneakyThrows
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

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        getLogger().info("Configurations loaded successfully!");

        Objects.requireNonNull(getCommand("screload")).setExecutor(new ReloadCommand(this));

        if (SpigotConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda = JDABuilder.createDefault(SpigotConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                            (SpigotConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                    SpigotConfig.DISCORD_ACTIVITY.get(String.class)));

            getLogger().info("Hooked into Discord successfully!");

        }

        if (SpigotConfig.STAFFCHAT.get(Boolean.class)) {

            Objects.requireNonNull(getCommand("scmute")).setExecutor(new MuteCommand(this));
            Objects.requireNonNull(getCommand("sctoggle")).setExecutor(new ToggleCommand(this));
            Objects.requireNonNull(getCommand("sc")).setExecutor(new StaffChatCommand(this));
            Objects.requireNonNull(getCommand("scafk")).setExecutor(new AFKCommand(this));
            getServer().getPluginManager().registerEvents(new JoinListener(this), this);
            getServer().getPluginManager().registerEvents(new it.frafol.cleanstaffchat.bukkit.staffchat.listeners.ChatListener(this), this);
            getServer().getPluginManager().registerEvents(new MoveListener(this), this);

            if (SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class) && SpigotConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new ChatListener(this));
            }

        }

        if (SpigotConfig.DONORCHAT.get(Boolean.class)) {

            Objects.requireNonNull(getCommand("dcmute")).setExecutor(new it.frafol.cleanstaffchat.bukkit.donorchat.commands.MuteCommand(this));
            Objects.requireNonNull(getCommand("dctoggle")).setExecutor(new it.frafol.cleanstaffchat.bukkit.donorchat.commands.ToggleCommand(this));
            Objects.requireNonNull(getCommand("dc")).setExecutor(new it.frafol.cleanstaffchat.bukkit.donorchat.commands.DonorChatCommand(this));
            getServer().getPluginManager().registerEvents(new it.frafol.cleanstaffchat.bukkit.donorchat.listeners.ChatListener(this), this);

            if (SpigotConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && SpigotConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.bukkit.donorchat.listeners.ChatListener(this));
            }

        }

        if (SpigotConfig.ADMINCHAT.get(Boolean.class)) {

            Objects.requireNonNull(getCommand("acmute")).setExecutor(new it.frafol.cleanstaffchat.bukkit.adminchat.commands.MuteCommand(this));
            Objects.requireNonNull(getCommand("actoggle")).setExecutor(new it.frafol.cleanstaffchat.bukkit.adminchat.commands.ToggleCommand(this));
            Objects.requireNonNull(getCommand("ac")).setExecutor(new it.frafol.cleanstaffchat.bukkit.adminchat.commands.AdminChatCommand(this));
            getServer().getPluginManager().registerEvents(new it.frafol.cleanstaffchat.bukkit.adminchat.listeners.ChatListener(this), this);

            if (SpigotConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && SpigotConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.bukkit.adminchat.listeners.ChatListener(this));
            }

        }

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

    public JDA getJda() {
        return jda;
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
        PlayerCache.getToggled_donor().clear();
        PlayerCache.getToggled_admin().clear();
        PlayerCache.getToggled_2_donor().clear();
        PlayerCache.getToggled_2_admin().clear();
        PlayerCache.getCooldown_discord().clear();
        PlayerCache.getMuted().clear();
        PlayerCache.getMuted_admin().clear();
        PlayerCache.getMuted_donor().clear();
        PlayerCache.getAfk().clear();

        getLogger().info("Successfully disabled.");
    }
}