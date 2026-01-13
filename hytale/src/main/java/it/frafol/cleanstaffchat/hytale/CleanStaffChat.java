package it.frafol.cleanstaffchat.hytale;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.enums.HytaleCommandsConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleDiscordConfig;
import it.frafol.cleanstaffchat.hytale.objects.TextFile;
import it.frafol.cleanstaffchat.hytale.staffchat.commands.ReloadCommand;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.ListChatListener;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.MoveListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CleanStaffChat extends JavaPlugin {

    private static JDA jda;
    private static CleanStaffChat instance;

    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile versionTextFile;

    public boolean updated = false;

    public CleanStaffChat(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {

        getLogger().at(Level.INFO).log("\n  ___  __    ____    __    _  _    ___    ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (    \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");
        //  getLogger().at(Level.INFO).log("Hytale Server Version: " + HytaleServer.get().getVersion());

        try {
            this.configTextFile = new TextFile(getDataDirectory(), "config.yml");
            this.messagesTextFile = new TextFile(getDataDirectory(), "messages.yml");
            this.discordTextFile = new TextFile(getDataDirectory(), "discord.yml");
            this.aliasesTextFile = new TextFile(getDataDirectory(), "aliases.yml");
            this.versionTextFile = new TextFile(getDataDirectory(), "version.yml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getLogger().at(Level.INFO).log("Configurations loaded successfully!");
    }

    @Override
    protected void start() {

        getCommandRegistry().registerCommand(new ReloadCommand(this));
        startJDA();

        if (Boolean.TRUE.equals(HytaleConfig.STAFFCHAT.get(Boolean.class))) {
            registerStaffChatCommands();
            JoinListener joinListener = new JoinListener(this);
            getEventRegistry().registerGlobal(PlayerConnectEvent.class, joinListener::handleJoin);
            getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, joinListener::handleQuit);
            ChatListener chatListener = new ChatListener(this);
            getEventRegistry().registerGlobal(PlayerChatEvent.class, chatListener::onChat);
            HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                    () -> new MoveListener(this).update(),
                    0,
                    500,
                    TimeUnit.MILLISECONDS
            );

            if (Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) && HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
                if (jda != null) jda.addEventListener(new ChatListener(this));
            }
        }

        if (Boolean.TRUE.equals(HytaleConfig.ADMINCHAT.get(Boolean.class))) {
            registerAdminChatCommands();
            it.frafol.cleanstaffchat.hytale.adminchat.listeners.ChatListener adminListener = new it.frafol.cleanstaffchat.hytale.adminchat.listeners.ChatListener(this);
            getEventRegistry().registerGlobal(PlayerChatEvent.class, adminListener::onChat);
        }

        if (Boolean.TRUE.equals(HytaleConfig.DONORCHAT.get(Boolean.class))) {
            try {
                registerDonorChatCommands();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            it.frafol.cleanstaffchat.hytale.donorchat.listeners.ChatListener donorListener = new it.frafol.cleanstaffchat.hytale.donorchat.listeners.ChatListener(this);
            getEventRegistry().registerGlobal(PlayerChatEvent.class, donorListener::onChat);
        }

        if (Boolean.TRUE.equals(HytaleConfig.MUTECHAT_MODULE.get(Boolean.class))) {
            try {
                registerMuteChatCommands();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            it.frafol.cleanstaffchat.hytale.general.listeners.ChatListener muteListener = new it.frafol.cleanstaffchat.hytale.general.listeners.ChatListener(this);
            getEventRegistry().registerGlobal(PlayerChatEvent.class, muteListener::onChat);
        }

        if (Boolean.TRUE.equals(HytaleConfig.STAFFLIST_MODULE.get(Boolean.class))) {
            registerStaffListCommands();
        }

        getLogger().at(Level.INFO).log("Plugin successfully enabled on Hytale!");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("Deleting instances...");
        if (HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && Boolean.FALSE.equals(HytaleConfig.WORKAROUND_KICK.get(Boolean.class))) {
            jda.shutdownNow();
        }
        instance = null;
        configTextFile = null;
        getLogger().at(Level.INFO).log("Successfully disabled.");
    }

    public void startJDA() {
        if (HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            try {
                jda = JDABuilder.createDefault(HytaleDiscordConfig.DISCORD_TOKEN.get(String.class))
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .build();
                getLogger().at(Level.INFO).log("Hooked into Discord successfully!");
            } catch (Exception e) {
                getLogger().at(Level.SEVERE).withCause(e).log("Invalid Discord configuration.");
            }
        }
    }

    private void registerStaffChatCommands() {
        try {
            for (HytaleCommandsConfig commandConfig : HytaleCommandsConfig.getStaffChatCommands()) {
                List<String> commandLabels = commandConfig.getStringList();
                if (commandLabels.isEmpty()) continue;
                AbstractCommand command = (AbstractCommand) commandConfig.getCommandClass()
                        .getDeclaredConstructors()[0].newInstance(
                        this,
                        commandLabels.get(0),
                        "Staff Chat Command",
                        commandLabels.subList(1, commandLabels.size())
                );
                Universe.get().getCommandRegistry().registerCommand(command);
            }
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to register Staff Chat commands.");
        }
    }

    private void registerStaffListCommands() {
        try {
            for (HytaleCommandsConfig commandsList : HytaleCommandsConfig.getStaffListCommands()) {
                List<String> commandLabels = commandsList.getStringList();
                if (commandLabels.isEmpty()) {
                    continue;
                }
                AbstractCommand command = (AbstractCommand) commandsList.getCommandClass()
                        .getDeclaredConstructors()[0].newInstance(
                        this,
                        commandLabels.get(0),
                        "Staff List Command",
                        commandLabels.subList(1, commandLabels.size())
                );
                Universe.get().getCommandRegistry().registerCommand(command);
            }
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to register Staff Chat commands.");
        }

        if (Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) &&
                Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))) {
            if (jda != null) {
                jda.addEventListener(new ListChatListener(this));
            }
        }
    }

    private void registerAdminChatCommands() {
        try {
            for (HytaleCommandsConfig commandConfig : HytaleCommandsConfig.getAdminChatCommands()) {
                List<String> commandLabels = commandConfig.getStringList();
                if (commandLabels.isEmpty()) continue;

                AbstractCommand command = (AbstractCommand) commandConfig.getCommandClass()
                        .getDeclaredConstructors()[0].newInstance(
                        this,
                        commandLabels.get(0),
                        "Admin Chat Command",
                        commandLabels.subList(1, commandLabels.size())
                );

                Universe.get().getCommandRegistry().registerCommand(command);
            }
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to register Staff Chat commands.");
        }
    }

    private void registerMuteChatCommands() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (HytaleCommandsConfig commandConfig : HytaleCommandsConfig.getMuteChatCommands()) {
            List<String> commandLabels = commandConfig.getStringList();
            if (commandLabels.isEmpty()) continue;

            AbstractCommand command = (AbstractCommand) commandConfig.getCommandClass()
                    .getDeclaredConstructors()[0].newInstance(
                    this,
                    commandLabels.get(0),
                    "Mute Chat Command",
                    commandLabels.subList(1, commandLabels.size())
            );

            Universe.get().getCommandRegistry().registerCommand(command);
        }
    }

    private void registerDonorChatCommands() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (HytaleCommandsConfig commandConfig : HytaleCommandsConfig.getDonorChatCommands()) {
            List<String> commandLabels = commandConfig.getStringList();
            if (commandLabels.isEmpty()) continue;

            AbstractCommand command = (AbstractCommand) commandConfig.getCommandClass()
                    .getDeclaredConstructors()[0].newInstance(
                    this,
                    commandLabels.get(0),
                    "Donor Chat Command",
                    commandLabels.subList(1, commandLabels.size())
            );

            Universe.get().getCommandRegistry().registerCommand(command);
        }
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

    public YamlFile getVersionTextFile() {
        return getInstance().versionTextFile.getConfig();
    }

    private void updateJDATask() {
        HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            if (getJda() == null) return;
            try {
                updateJDA();
            } catch (Exception e) {
                getLogger().at(Level.SEVERE).log("[CleanStaffChat] Errore durante l'update del JDA: " + e.getMessage());
            }
        }, 1, 30, TimeUnit.SECONDS);
    }

    public void updateJDA() {

        if (!HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            return;
        }

        if (jda == null) {
            getLogger().at(Level.SEVERE).log("Fatal error while updating JDA. Please report this error to https://dsc.gg/futuredevelopment.");
            return;
        }

        jda.getPresence().setActivity(Activity.of(
                Activity.ActivityType.valueOf(HytaleDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                HytaleDiscordConfig.DISCORD_ACTIVITY.get(String.class)
                        .replace("%players%", String.valueOf(Universe.get().getPlayers().size()))
        ));
    }

    public static CleanStaffChat getInstance() {
        return instance;
    }

    public static JDA getJda() {
        return jda;
    }
}