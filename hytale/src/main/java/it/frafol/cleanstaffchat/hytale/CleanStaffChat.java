package it.frafol.cleanstaffchat.hytale;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.enums.HytaleCommandsConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleDiscordConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleVersion;
import it.frafol.cleanstaffchat.hytale.objects.*;
import it.frafol.cleanstaffchat.hytale.staffchat.commands.ReloadCommand;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.ListChatListener;
import it.frafol.cleanstaffchat.hytale.staffchat.listeners.MoveListener;
import net.byteflux.libby.HytaleLibraryManager;
import net.byteflux.libby.Library;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CleanStaffChat extends JavaPlugin {

    private static JDA jda;
    private static CleanStaffChat instance;

    private MessagingSystem chatSystem = null;

    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile versionTextFile;

    public CleanStaffChat(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void start() {

        getLogger().at(Level.INFO).log("""
                
                  ___  __    ____    __    _  _    ___   ___\s
                 / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)
                ( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__\s
                 \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)
                """);
        //getLogger().at(Level.INFO).log("Hytale Server Version: " + HytaleServer.get());

        loadLibraries();
        loadFiles();
        updateConfig();
        getLogger().at(Level.INFO).log("Configurations loaded successfully!");

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

        if (Boolean.TRUE.equals(HytaleConfig.MYSQL_ENABLED.get(Boolean.class))) {
            chatSystem = new MessagingSystem(
                    HytaleConfig.MYSQL_IP.get(String.class),
                    HytaleConfig.MYSQL_PORT.get(Integer.class),
                    HytaleConfig.MYSQL_DATABASE.get(String.class),
                    HytaleConfig.MYSQL_USER.get(String.class),
                    HytaleConfig.MYSQL_PASSWORD.get(String.class),
                    HytaleConfig.SERVER_NAME.color());
            handleSQL();
        }

        if (Boolean.TRUE.equals(HytaleConfig.UPDATE_CHECK.get(Boolean.class)))
            UpdateCheck.checkForUpdates(this, getVersionFromJson(), "cmkcxg67b000g01s6ewk287pn");
        getLogger().at(Level.INFO).log("Plugin successfully enabled on Hytale!");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("Deleting instances...");
        if (HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            jda.shutdownNow();
        }
        instance = null;
        configTextFile = null;
        getLogger().at(Level.INFO).log("Successfully disabled.");
    }

    public void startJDA() {
        if (Boolean.TRUE.equals(HytaleConfig.MYSQL_ENABLED.get(Boolean.class))) return;
        if (HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            try {
                JDALogger.setFallbackLoggerEnabled(false);
                jda = JDABuilder.createDefault(HytaleDiscordConfig.DISCORD_TOKEN.get(String.class))
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .build();
                updateJDATask();
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
                        commandLabels.getFirst(),
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
                        commandLabels.getFirst(),
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
                        commandLabels.getFirst(),
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
                    commandLabels.getFirst(),
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
                    commandLabels.getFirst(),
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

    private void loadLibraries() {
        HytaleLibraryManager hytaleLibraryManager = new HytaleLibraryManager(this);
        Library yaml = Library.builder().groupId("me{}carleslc{}Simple-YAML").artifactId("Simple-Yaml").version("1.8.4").url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar").build();
        Library discord = Library.builder().groupId("net{}dv8tion").artifactId("JDA").version("6.3.0").url("https://github.com/discord-jda/JDA/releases/download/v6.3.0/JDA-6.3.0-withDependencies-min.jar").build();
        hytaleLibraryManager.addMavenCentral();
        hytaleLibraryManager.addJitPack();
        hytaleLibraryManager.loadLibrary(discord);
        hytaleLibraryManager.loadLibrary(yaml);
    }

    private void updateJDATask() {
        HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            if (getJda() == null) return;
            try {
                updateJDA();
            } catch (Exception e) {
                getLogger().at(Level.SEVERE).log("Error during JDA update: " + e.getMessage());
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

    private void handleSQL() {
        HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            chatSystem.pollMessages((channel, dbPayload) -> {
                String[] parts = dbPayload.split(":::", 3);
                if (parts.length < 3) return;
                UUID originUuid = UUID.fromString(parts[0]);
                String originName = parts[1];
                String formattedContent = parts[2];
                String perm = switch (channel.toUpperCase()) {
                    case "ADMIN" -> HytaleConfig.ADMINCHAT_USE_PERMISSION.get(String.class);
                    case "DONOR" -> HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class);
                    default -> HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
                };
                Message hytaleMsg = ChatColor.color(originUuid, originName, formattedContent);
                Universe.get().getWorlds().values().forEach(world -> {
                    for (PlayerRef ref : world.getPlayerRefs()) {
                        if (PermissionsUtil.hasPermission(ref.getUuid(), perm)
                                && !PlayerCache.getToggled().contains(ref.getUuid())) {
                            ref.sendMessage(hytaleMsg);
                        }
                    }
                });
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateConfig() {
        if (!getVersionFromJson().equals(HytaleVersion.VERSION.get(String.class))) {
            getLogger().at(Level.INFO).log("Creating new configurations...");
            YamlUpdater.create(new File(getDataDirectory() + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/hytale/hytale/src/main/resources/cleansc_config.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(getDataDirectory() + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/hytale/hytale/src/main/resources/cleansc_messages.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(getDataDirectory() + "/discord.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/hytale/hytale/src/main/resources/cleansc_discord.yml"))
                    .backup(true)
                    .update();
            YamlUpdater.create(new File(getDataDirectory() + "/aliases.yml"), FileUtils.findFile("https://raw.githubusercontent.com/frafol/CleanStaffChat/refs/heads/hytale/hytale/src/main/resources/cleansc_aliases.yml"))
                    .backup(true)
                    .update();
            versionTextFile.getConfig().set("version", getVersionFromJson());
            try {
                versionTextFile.getConfig().save();
            } catch (Exception ignored) {}
            loadFiles();
        }
    }

    public String getVersionFromJson() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.json")) {
            if (is == null) return HytaleVersion.VERSION.get(String.class);
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                if (jsonObject.has("Version")) return jsonObject.get("Version").getAsString();
            }
        } catch (Exception ignored) {}
        return HytaleVersion.VERSION.get(String.class);
    }

    private void loadFiles() {
        Path dataFolder = getDataDirectory();
        try {
            this.configTextFile = new TextFile(dataFolder, "config.yml", "cleansc_config.yml");
            this.messagesTextFile = new TextFile(dataFolder, "messages.yml", "cleansc_messages.yml");
            this.discordTextFile = new TextFile(dataFolder, "discord.yml", "cleansc_discord.yml");
            this.aliasesTextFile = new TextFile(dataFolder, "aliases.yml", "cleansc_aliases.yml");
            this.versionTextFile = new TextFile(dataFolder, "version.yml", "cleansc_version.yml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CleanStaffChat getInstance() {
        return instance;
    }

    public MessagingSystem getChatSystem() {
        return chatSystem;
    }

    public static JDA getJda() {
        return jda;
    }
}