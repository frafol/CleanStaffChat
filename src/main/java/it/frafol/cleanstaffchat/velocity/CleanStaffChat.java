package it.frafol.cleanstaffchat.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.Commands.MuteCommand;
import it.frafol.cleanstaffchat.velocity.Commands.ReloadCommand;
import it.frafol.cleanstaffchat.velocity.Commands.StaffChatCommand;
import it.frafol.cleanstaffchat.velocity.Commands.ToggleCommand;
import it.frafol.cleanstaffchat.velocity.Listeners.ChatListener;
import it.frafol.cleanstaffchat.velocity.Listeners.JoinListener;
import it.frafol.cleanstaffchat.velocity.Listeners.ServerListener;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.objects.TextFile;
import lombok.Getter;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.logging.Logger;

@Getter
@Plugin(
        id = "cleanstaffchat",
        name = "CleanStaffChat",
        version = "1.1.1",
        url = "github.com/frafol",
        authors = "frafol"
)
public class CleanStaffChat {

    private final Metrics.Factory metricsFactory;
    private final ProxyServer server;
    private final Logger logger;
    private final Path path;
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
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("sc")
                .aliases("staffchat")
                .aliases("cleansc")
                .aliases("cleanstaffchat")
                .build(), new StaffChatCommand(this));
        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("scmute")
                .aliases("staffchatmute")
                .aliases("cleanscmute")
                .aliases("cleanstaffchatmute")
                .build(), new MuteCommand(this));
        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("screload")
                .aliases("staffchatreload")
                .aliases("cleanscreload")
                .aliases("cleanstaffchatreload")
                .build(), new ReloadCommand(this));
        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("sctoggle")
                .aliases("staffchattoggle")
                .aliases("staffchatoggle")
                .aliases("cleansctoggle")
                .aliases("cleanstaffchattoggle")
                .build(), new ToggleCommand(this));
        getLogger().info("§7Commands registered §asuccessfully§7!");

        server.getEventManager().register(this, new JoinListener(this));
        server.getEventManager().register(this, new ServerListener(this));
        server.getEventManager().register(this, new ChatListener(this));
        getLogger().info("§7Listeners registered §asuccessfully§7!");

        configTextFile = new TextFile(path, "config.yml");
        getLogger().info("§7Configurations loaded §asuccessfully§7!");


        if (VelocityConfig.STATS.get(Boolean.class)) {

            metricsFactory.make(this, 16447);

            getLogger().info("§7Metrics loaded §asuccessfully§7!");

        }

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
        PlayerCache.getMuted().clear();

        logger.info("§7Successfully §cdisabled§7.");
    }
}