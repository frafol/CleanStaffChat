package it.frafol.cleanstaffchat.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.velocity.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.velocity.enums.VelocityCommandsConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityDiscordConfig;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.objects.TextFile;
import it.frafol.cleanstaffchat.velocity.staffchat.commands.*;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ServerListener;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import java.nio.file.Path;
import java.util.logging.Logger;

@Getter
@Plugin(
        id = "cleanstaffchat",
        name = "CleanStaffChat",
        version = "1.6",
        url = "github.com/frafol",
        authors = "frafol"
)
public class CleanStaffChat {

    private final Metrics.Factory metricsFactory;
    private final ProxyServer server;
    private final Logger logger;
    private final Path path;
    private JDA jda;
    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @Inject
    public CleanStaffChat(ProxyServer server, Logger logger, @DataDirectory Path path, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.path = path;
        this.metricsFactory = metricsFactory;
    }


    @Inject
    public PluginContainer container;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws LoginException {
        instance = this;
        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");
        discordTextFile = new TextFile(path, "discord.yml");
        aliasesTextFile = new TextFile(path, "aliases.yml");
        getLogger().info("§7Configurations loaded §dsuccessfully§7!");

        if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda = JDABuilder.createDefault(VelocityDiscordConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                            (VelocityDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                    VelocityDiscordConfig.DISCORD_ACTIVITY.get(String.class)));

            if (getServer().getPluginManager().isLoaded("serverutils")) {

                getLogger().warning("\n§f\n§e§lWARNING!" +
                        "\n§f\n§7Integration on Discord may give you many problems if you reload the plugin with ServerUtils." +
                        "\n§7Consider performing a §d§lTOTAL RESTART to prevent issues!\n");

            }

            getLogger().info("§7Hooked into Discord §dsuccessfully§7!");

        }

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("screload")
                .aliases("staffchatreload", "staffreload", "cleanscreload", "cleanstaffchatreload")
                .build(), new ReloadCommand(this));

        if (VelocityConfig.STAFFCHAT.get(Boolean.class)) {

            final String[] aliases_staffchat = VelocityCommandsConfig.STAFFCHAT.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT.getStringList().get(0))
                    .aliases(aliases_staffchat)
                    .build(), new StaffChatCommand(this));


            final String[] aliases_staffchatmute = VelocityCommandsConfig.STAFFCHAT_MUTE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT_MUTE.getStringList().get(0))
                    .aliases(aliases_staffchatmute)
                    .build(), new MuteCommand(this));


            final String[] aliases_staffchattoggle = VelocityCommandsConfig.STAFFCHAT_TOGGLE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT_TOGGLE.getStringList().get(0))
                    .aliases(aliases_staffchattoggle)
                    .build(), new ToggleCommand(this));


            final String[] aliases_staffchatafk = VelocityCommandsConfig.STAFFCHAT_AFK.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.STAFFCHAT_AFK.getStringList().get(0))
                    .aliases(aliases_staffchatafk)
                    .build(), new AFKCommand(this));

            server.getEventManager().register(this, new JoinListener(this));
            server.getEventManager().register(this, new ServerListener(this));
            server.getEventManager().register(this, new ChatListener(this));

            if (VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new ChatListener(this));
            }

        }

        if (VelocityConfig.DONORCHAT.get(Boolean.class)) {

            final String[] aliases_donorchat = VelocityCommandsConfig.DONORCHAT.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.DONORCHAT.getStringList().get(0))
                    .aliases(aliases_donorchat)
                    .build(), new DonorChatCommand(this));

            final String[] aliases_donormute = VelocityCommandsConfig.DONORCHAT_MUTE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.DONORCHAT_MUTE.getStringList().get(0))
                    .aliases(aliases_donormute)
                    .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.MuteCommand(this));

            final String[] aliases_donortoggle = VelocityCommandsConfig.DONORCHAT_TOGGLE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.DONORCHAT_TOGGLE.getStringList().get(0))
                    .aliases(aliases_donortoggle)
                    .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.ToggleCommand(this));

            server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));

            if (VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));
            }

        }

        if (VelocityConfig.ADMINCHAT.get(Boolean.class)) {

            final String[] aliases_adminchat = VelocityCommandsConfig.ADMINCHAT.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.ADMINCHAT.getStringList().get(0))
                    .aliases(aliases_adminchat)
                    .build(), new AdminChatCommand(this));

            final String[] aliases_adminchatmute = VelocityCommandsConfig.ADMINCHAT_MUTE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.ADMINCHAT_MUTE.getStringList().get(0))
                    .aliases(aliases_adminchatmute)
                    .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.MuteCommand(this));

            final String[] aliases_adminchattoggle = VelocityCommandsConfig.ADMINCHAT_TOGGLE.getStringList().toArray(new String[0]);

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder(VelocityCommandsConfig.ADMINCHAT_TOGGLE.getStringList().get(0))
                    .aliases(aliases_adminchattoggle)
                    .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.ToggleCommand(this));

            server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));

            if (VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));
            }

        }

        if (VelocityConfig.STATS.get(Boolean.class)) {

            metricsFactory.make(this, 16447);

            getLogger().info("§7Metrics loaded §dsuccessfully§7!");

        }

        getLogger().warning("Some functions are not available on Velocity in 1.19+ clients, this is due to Mojang's self-moderation.");

        if (VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
            new UpdateCheck(this).getVersion(version -> {
                if (container.getDescription().getVersion().isPresent()) {
                    if (!container.getDescription().getVersion().get().equals(version)) {
                        getLogger().warning("There is a new update available, download it on SpigotMC!");
                    }
                }
            });
        }

        getLogger().info("§7Plugin successfully &denabled§7!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        getLogger().info("Deleting instances...");
        instance = null;
        configTextFile = null;

        logger.info("§7Clearing lists...");
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

        logger.info("§7Successfully §ddisabled§7.");
    }
}