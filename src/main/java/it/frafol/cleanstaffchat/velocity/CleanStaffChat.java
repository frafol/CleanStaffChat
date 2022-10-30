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
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
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
        version = "1.3.2",
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
        getLogger().info("§7Configurations loaded §asuccessfully§7!");

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("screload")
                .aliases("staffchatreload")
                .aliases("cleanscreload")
                .aliases("staffreload")
                .aliases("cleanstaffchatreload")
                .build(), new ReloadCommand(this));

        if (VelocityConfig.STAFFCHAT.get(Boolean.class)) {

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("sc")
                    .aliases("staffchat")
                    .aliases("cleansc")
                    .aliases("staff")
                    .aliases("cleanstaffchat")
                    .build(), new StaffChatCommand(this));
            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("scmute")
                    .aliases("staffchatmute")
                    .aliases("cleanscmute")
                    .aliases("staffmute")
                    .aliases("cleanstaffchatmute")
                    .build(), new MuteCommand(this));
            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("sctoggle")
                    .aliases("staffchattoggle")
                    .aliases("staffchatoggle")
                    .aliases("cleansctoggle")
                    .aliases("stafftoggle")
                    .aliases("cleanstaffchattoggle")
                    .build(), new ToggleCommand(this));
            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("scafk")
                    .aliases("staffchatafk")
                    .aliases("staffafk")
                    .aliases("cleanscafk")
                    .aliases("cleanstaffchatafk")
                    .build(), new AFKCommand(this));

            server.getEventManager().register(this, new JoinListener(this));
            server.getEventManager().register(this, new ServerListener(this));
            server.getEventManager().register(this, new ChatListener(this));

        }

        if (VelocityConfig.DONORCHAT.get(Boolean.class)) {

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("dc")
                    .aliases("donorchat")
                    .aliases("donor")
                    .build(), new DonorChatCommand(this));
            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("dcmute")
                    .aliases("donorchatmute")
                    .aliases("donormute")
                    .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.MuteCommand(this));
            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("dctoggle")
                    .aliases("donorchattoggle")
                    .aliases("donorchatoggle")
                    .aliases("donortoggle")
                    .build(), new it.frafol.cleanstaffchat.velocity.donorchat.commands.ToggleCommand(this));

            server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));

        }

        if (VelocityConfig.ADMINCHAT.get(Boolean.class)) {

            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("ac")
                    .aliases("adminchat")
                    .aliases("admin")
                    .build(), new AdminChatCommand(this));
            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("acmute")
                    .aliases("adminchatmute")
                    .aliases("adminmute")
                    .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.MuteCommand(this));
            server.getCommandManager().register(server.getCommandManager()
                    .metaBuilder("actoggle")
                    .aliases("adminchattoggle")
                    .aliases("adminchatoggle")
                    .aliases("admintoggle")
                    .build(), new it.frafol.cleanstaffchat.velocity.adminchat.commands.ToggleCommand(this));

            server.getEventManager().register(this, new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));

        }

        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda = JDABuilder.createDefault(VelocityConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf
                            (VelocityConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                    VelocityConfig.DISCORD_ACTIVITY.get(String.class)));

            if (VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {
                jda.addEventListener(new ChatListener(this));
            }

            if (VelocityConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {
                jda.addEventListener(new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));
            }

        }

        if (VelocityConfig.STATS.get(Boolean.class)) {

            metricsFactory.make(this, 16447);

            getLogger().info("§7Metrics loaded §asuccessfully§7!");

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

        getLogger().info("§7Plugin successfully §aenabled§7!");
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

        logger.info("§7Successfully §cdisabled§7.");
    }
}