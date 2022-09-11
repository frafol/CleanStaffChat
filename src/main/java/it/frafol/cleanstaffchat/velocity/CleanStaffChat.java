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
        version = "1.0.0",
        url = "github.com/frafol",
        authors = "frafol"
)
public class CleanStaffChat {

    private final ProxyServer server;
    private final Logger logger;
    private final Path path;
    private TextFile configTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    @Inject
    public CleanStaffChat(ProxyServer server, Logger logger, @DataDirectory Path path) {
        this.server = server;
        this.logger = logger;
        this.path = path;
    }

    @Inject
    public PluginContainer container;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        logger.info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");

        logger.info("§7Registering commands...");
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
        logger.info("§7Commands registered §asuccessfully§7!");

        logger.info("§7Registering listeners...");
        server.getEventManager().register(this, new JoinListener(this));
        server.getEventManager().register(this, new ServerListener(this));
        server.getEventManager().register(this, new ChatListener(this));
        logger.info("§7Listeners registered §asuccessfully§7!");

        logger.info("§7Loading configurations...");
        configTextFile = new TextFile(path, "config.yml");
        logger.info("§7Configurations loaded §asuccessfully§7!");

        if (VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
            new UpdateCheck(this).getVersion(version -> {
                if (!container.getDescription().getVersion().equals(version)) {
                    getLogger().warning("There is a new update available, download it on SpigotMC!");
                }
            });
        }

        logger.info("§7Plugin successfully §aenabled§7, enjoy!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("§7Clearing lists...");
        PlayerCache.getToggled_2().clear();
        PlayerCache.getToggled().clear();
        PlayerCache.getMuted().clear();
        logger.info("§7Successfully §cdisabled§7.");
    }
}