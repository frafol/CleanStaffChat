package it.frafol.cleanstaffchat.bukkit;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.tchristofferson.configupdater.ConfigUpdater;
import it.frafol.cleanstaffchat.bukkit.channel.ChannelCommand;
import it.frafol.cleanstaffchat.bukkit.channel.ChannelListener;
import it.frafol.cleanstaffchat.bukkit.channel.ChannelMuteCommand;
import it.frafol.cleanstaffchat.bukkit.channel.ChannelToggleCommand;
import it.frafol.cleanstaffchat.bukkit.channel.ChatChannel;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotCommandsConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotVersion;
import it.frafol.cleanstaffchat.bukkit.general.commands.MuteChatCommand;
import it.frafol.cleanstaffchat.bukkit.objects.DiscordBridge;
import it.frafol.cleanstaffchat.bukkit.objects.LibertyBansUtil;
import it.frafol.cleanstaffchat.bukkit.objects.NoChatListener;
import it.frafol.cleanstaffchat.bukkit.objects.PluginMessageReceiver;
import it.frafol.cleanstaffchat.bukkit.objects.TextFile;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.AFKCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.DebugCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.ReloadCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.StaffListCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.ListChatListener;
import it.frafol.cleanstaffchat.bukkit.staffchat.listeners.MoveListener;
import litebans.api.Database;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

@Getter
public class CleanStaffChat extends JavaPlugin {

    private static final String PROXY_CHANNEL = "cleansc:cancel";
    private static final String RELEASE_URL = "https://github.com/frafol/CleanStaffChat/releases/download/release/CleanStaffChat.jar";
    private static final List<String> CONFIG_FILES = java.util.Arrays.asList("config.yml", "messages.yml", "discord.yml", "aliases.yml");

    @Getter
    private static CleanStaffChat instance;

    private JDA jda;

    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile versionTextFile;

    private boolean update = false;
    private String updatedVersion = "";
    public boolean updated = false;

    private boolean miniMessageAvailable;
    private CommandMap commandMap;

    @SneakyThrows
    @Override
    public void onEnable() {
        instance = this;

        loadLibraries();
        printBanner();
        loadConfigurations();
        miniMessageAvailable = detectMiniMessage();

        register(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new DebugCommand(), this);

        if (enableProxyMode()) return;

        if (SpigotConfig.UPDATE_CHECK.get(Boolean.class)) {
            UniversalScheduler.getScheduler(this).runTaskTimer(this::checkForUpdates, 20L, 10 * 3600 * 20L);
        }

        startJDA();
        logHooks();
        for (ChatChannel channel : ChatChannel.values()) registerChannel(channel);
        registerStaffExtras();

        if (SpigotConfig.STAFFLIST_MODULE.get(Boolean.class)) registerStaffList();
        if (SpigotConfig.MUTECHAT_MODULE.get(Boolean.class)) {
            registerCommand(SpigotCommandsConfig.MUTECHAT, MuteChatCommand::new);
            getServer().getPluginManager().registerEvents(
                    new it.frafol.cleanstaffchat.bukkit.general.listeners.ChatListener(this), this);
        }

        if (SpigotConfig.STATS.get(Boolean.class) && !getDescription().getVersion().contains("alpha")) {
            new Metrics(this, 16448);
            getLogger().info("Metrics loaded successfully!");
        }

        getLogger().info("Plugin successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Deleting instances...");
        getServer().getMessenger().unregisterIncomingPluginChannel(this, PROXY_CHANNEL);
        if (jda != null) jda.shutdownNow();
        instance = null;
        configTextFile = null;
        getLogger().info("Successfully disabled.");
    }

    private void loadLibraries() {
        final BukkitLibraryManager manager = new BukkitLibraryManager(this);
        manager.addMavenCentral();
        manager.addJitPack();
        final Relocation yamlRelocation = new Relocation("yaml", "it{}frafol{}libs{}yaml");
        final Library.Builder yaml = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.4")
                .relocate(yamlRelocation);

        try {
            manager.loadLibrary(yaml.build());
        } catch (RuntimeException exception) {
            getLogger().severe("Failed to load Simple-YAML, trying to download it from GitHub...");
            manager.loadLibrary(yaml
                    .url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
                    .build());
        }

        manager.loadLibrary(Library.builder()
                .groupId("com{}tchristofferson")
                .artifactId("ConfigUpdater")
                .version("2.1-SNAPSHOT")
                .relocate("updater", "it{}frafol{}libs{}updater")
                .url("https://github.com/frafol/Config-Updater/releases/download/compile/ConfigUpdater-2.1-SNAPSHOT.jar")
                .build());

        manager.loadLibrary(Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("6.7.0")
                .relocate("kotlin", "it{}frafol{}libs{}kotlin")
                .url("https://github.com/discord-jda/JDA/releases/download/v6.7.0/JDA-6.7.0-withDependencies-min.jar")
                .build());

        manager.loadLibrary(Library.builder()
                .groupId("com{}github{}Anon8281")
                .artifactId("UniversalScheduler")
                .version("0.1.6")
                .relocate("scheduler", "it{}frafol{}libs{}scheduler")
                .build());
    }

    private void printBanner() {
        getLogger().info("\n  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");
        getLogger().info("Server version: " + getServer().getBukkitVersion());
    }

    @SneakyThrows
    private void loadConfigurations() {
        versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");

        if (!getDescription().getVersion().equals(SpigotVersion.VERSION.get(String.class))) {
            getLogger().info("Creating new configurations...");
            try {
                for (String fileName : CONFIG_FILES) {
                    ConfigUpdater.update(this, fileName, new File(getDataFolder(), fileName), Collections.emptyList());
                }
            } catch (IOException exception) {
                getLogger().severe("Unable to update the files. Are you on Windows?");
            }

            versionTextFile.getConfig().set("version", getDescription().getVersion());
            versionTextFile.getConfig().save();
        }

        configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
        messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");
        discordTextFile = new TextFile(getDataFolder().toPath(), "discord.yml");
        aliasesTextFile = new TextFile(getDataFolder().toPath(), "aliases.yml");
        getLogger().info("Configurations loaded successfully!");
    }

    /** Kick/double-message workaround: the plugin only cancels chat, driven by the proxy. */
    private boolean enableProxyMode() {
        final String mode;
        if (SpigotConfig.WORKAROUND_KICK.get(Boolean.class)) mode = "Kick Workaround";
        else if (SpigotConfig.DOUBLE_MESSAGE.get(Boolean.class)) mode = "Double Message Workaround";
        else return false;
        getServer().getMessenger().registerIncomingPluginChannel(this, PROXY_CHANNEL, new PluginMessageReceiver());
        getServer().getPluginManager().registerEvents(new NoChatListener(), this);
        getLogger().info("Successfully enabled in Proxy mode (" + mode + ")!");
        return true;
    }

    private void logHooks() {
        if (isPremiumVanish()) getLogger().info("Hooked into PremiumVanish successfully!");
        if (isSuperVanish()) getLogger().info("Hooked into SuperVanish successfully!");
        if (isVanishPP()) getLogger().info("Hooked into Vanish++ successfully!");
        if (getPAPI()) getLogger().info("Hooked into PlaceholderAPI successfully!");
        if (getMiniPlaceholders()) getLogger().info("Hooked into MiniPlaceholders successfully!");
    }

    private void registerChannel(ChatChannel channel) {
        if (!channel.isEnabled()) return;
        registerCommand(channel.aliases.main, (p, name, usage, aliases) -> new ChannelCommand(p, channel, name, usage, aliases));
        registerCommand(channel.aliases.mute, (p, name, usage, aliases) -> new ChannelMuteCommand(p, channel, name, usage, aliases));
        registerCommand(channel.aliases.toggle, (p, name, usage, aliases) -> new ChannelToggleCommand(p, channel, name, usage, aliases));
        final ChannelListener listener = new ChannelListener(this, channel);
        getServer().getPluginManager().registerEvents(listener, this);
        if (jda != null && DiscordBridge.isActive(channel.settings.discordModule)) {
            jda.addEventListener(listener);
        }
    }

    private void registerStaffExtras() {
        if (!ChatChannel.STAFF.isEnabled()) return;
        registerCommand(SpigotCommandsConfig.STAFFCHAT_AFK, AFKCommand::new);
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new MoveListener(this), this);
    }

    private void registerStaffList() {
        final PluginManager pluginManager = getServer().getPluginManager();
        final boolean luckPerms = pluginManager.getPlugin("LuckPerms") != null;
        if (!luckPerms && pluginManager.getPlugin("UltraPermissions") == null) {
            getLogger().warning("You need LuckPerms or UltraPermissions to use the StaffList.");
            return;
        }
        registerCommand(SpigotCommandsConfig.STAFFLIST, StaffListCommand::new);
        if (luckPerms && jda != null && DiscordBridge.isActive(SpigotConfig.STAFFCHAT_DISCORD_MODULE)) {
            jda.addEventListener(new ListChatListener(this));
        }
    }

    private void register(CommandBase command) {
        getCommandMap().register(getName().toLowerCase(), command);
    }

    private void registerCommand(SpigotCommandsConfig alias, CommandBase.Factory factory) {
        final List<String> labels = alias.getStringList();
        if (labels.isEmpty()) return;
        register(factory.create(this, labels.get(0), "", labels.subList(1, labels.size())));
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        if (commandMap == null) {
            final Field field = getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(getServer());
        }
        return commandMap;
    }

    public void startJDA() {
        if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) return;
        try {
            disableFallbackLogger();
            jda = JDABuilder.createDefault(SpigotDiscordConfig.DISCORD_TOKEN.get(String.class))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .build();
        } catch (ExceptionInInitializerError | RuntimeException exception) {
            getLogger().severe("Invalid Discord configuration, please check your discord.yml file.");
            return;
        }
        UniversalScheduler.getScheduler(this).runTaskTimerAsynchronously(this::updateJDA, 1, 20 * 30);
        getLogger().info("Hooked into Discord successfully!");
    }

    private void disableFallbackLogger() {
        try {
            JDALogger.setFallbackLoggerEnabled(false);
        } catch (NoSuchMethodError ignored) {}
    }

    public void updateJDA() {
        if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) return;
        if (jda == null) {
            getLogger().severe("Fatal error while updating JDA. Please report this error to https://dsc.gg/futuredevelopment.");
            return;
        }
        final String type = SpigotDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class);
        if (type.equals("NULL")) return;
        jda.getPresence().setActivity(Activity.of(
                Activity.ActivityType.valueOf(type.toUpperCase()),
                SpigotDiscordConfig.DISCORD_ACTIVITY.get(String.class)
                        .replace("%players%", String.valueOf(getServer().getOnlinePlayers().size()))));
    }

    private void checkForUpdates() {
        new UpdateCheck(this).getVersion(latest -> {
            final int comparison = compareVersions(getDescription().getVersion(), latest);
            if (comparison < 0 && !updated) {
                if (SpigotConfig.AUTO_UPDATE.get(Boolean.class)) {
                    autoUpdate();
                    return;
                }
                update = true;
                updatedVersion = latest;
                getLogger().warning("There is a new update available, download it on SpigotMC!");
            }
            if (comparison > 0) {
                getLogger().warning("You are using a development version, please report any bugs!");
            }
        });
    }

    public void autoUpdate() {
        final File outputFile = new File("./plugins/", "CleanStaffChat.jar");
        try (InputStream inputStream = new URL(RELEASE_URL).openStream()) {
            Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            updated = true;
            getLogger().warning("CleanStaffChat successfully updated, a restart is required.");
        } catch (IOException exception) {
            getLogger().severe("Unable to update the plugin, please download the latest version manually. Are you on Windows?");
        }
    }

    /** Compares dotted versions numerically ("1.17.10" is newer than "1.17.9"). */
    static int compareVersions(String first, String second) {
        final String[] a = first.split("\\.");
        final String[] b = second.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            final int left = i < a.length ? versionPart(a[i]) : 0;
            final int right = i < b.length ? versionPart(b[i]) : 0;
            if (left != right) return Integer.compare(left, right);
        }
        return 0;
    }

    private static int versionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("\\D", ""));
        } catch (NumberFormatException exception) {
            return 0;
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

    private boolean isPluginPresent(String name) {
        return getServer().getPluginManager().getPlugin(name) != null;
    }

    public boolean isPremiumVanish() {
        return SpigotConfig.PREMIUMVANISH.get(Boolean.class) && isPluginPresent("PremiumVanish");
    }

    public boolean isSuperVanish() {
        return SpigotConfig.SUPERVANISH.get(Boolean.class) && isPluginPresent("SuperVanish");
    }

    public boolean isVanishPP() {
        return SpigotConfig.VANISHPP.get(Boolean.class) && isPluginPresent("Vanishpp");
    }

    public boolean getPAPI() {
        return isPluginPresent("PlaceholderAPI");
    }

    public boolean getMiniPlaceholders() {
        return isPluginPresent("MiniPlaceholders") && SpigotConfig.MINIPLACEHOLDERS.get(Boolean.class);
    }

    /** True when the MiniMessage classes are available and the feature is enabled in config.yml. */
    public boolean supportsMiniMessage() {
        return miniMessageAvailable && SpigotConfig.MINIMESSAGE.get(Boolean.class);
    }

    private boolean detectMiniMessage() {
        try {
            MiniMessage.miniMessage().deserialize("supports");
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    /** True when an external punishment plugin (LiteBans / LibertyBans) says the player is muted. */
    public boolean isMuted(Player player) {
        if (isPluginPresent("LiteBans")) {
            final InetSocketAddress address = player.getAddress();
            final String ip = address == null ? null : address.getAddress().getHostAddress();
            return Database.get().isPlayerMuted(player.getUniqueId(), ip);
        }
        if (isPluginPresent("LibertyBans")) return LibertyBansUtil.isMuted(player.getUniqueId());
        return false;
    }
}
