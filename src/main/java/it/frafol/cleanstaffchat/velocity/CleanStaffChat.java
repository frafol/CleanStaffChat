package it.frafol.cleanstaffchat.velocity;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.velocity.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.velocity.enums.VelocityCommandsConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityDiscordConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityRedis;
import it.frafol.cleanstaffchat.velocity.hooks.RedisListener;
import it.frafol.cleanstaffchat.velocity.objects.JdaBuilder;
import it.frafol.cleanstaffchat.velocity.objects.TextFile;
import it.frafol.cleanstaffchat.velocity.staffchat.commands.*;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ChatListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.JoinListener;
import it.frafol.cleanstaffchat.velocity.staffchat.listeners.ServerListener;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import java.nio.file.Path;

@Getter
@Plugin(
        id = "cleanstaffchat",
        name = "CleanStaffChat",
        version = "1.8.1",
        dependencies = {@Dependency(id = "redisbungee", optional = true)},
        url = "github.com/frafol",
        authors = "frafol"
)
public class CleanStaffChat {

    private final Metrics.Factory metricsFactory;
    private final ProxyServer server;
    private final Logger logger;
    private final Path path;
    private JdaBuilder jda;
    private TextFile configTextFile;
    private TextFile messagesTextFile;
    private TextFile discordTextFile;
    private TextFile aliasesTextFile;
    private TextFile redisTextFile;
    private static CleanStaffChat instance;

    public static CleanStaffChat getInstance() {
        return instance;
    }

    public static String Version = "1.8.1";

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

        VelocityLibraryManager<CleanStaffChat> velocityLibraryManager = new VelocityLibraryManager<>(getLogger(), path, getServer().getPluginManager(), this);

        Library yaml = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.3")
                .build();

        Library discord = Library.builder()
                .groupId("net{}dv8tion")
                .artifactId("JDA")
                .version("5.0.0-beta.3")
                .url("https://github.com/DV8FromTheWorld/JDA/releases/download/v5.0.0-beta.3/JDA-5.0.0-beta.3-withDependencies-min.jar")
                .build();

        velocityLibraryManager.addMavenCentral();
        velocityLibraryManager.addJitPack();
        velocityLibraryManager.loadLibrary(yaml);
        velocityLibraryManager.loadLibrary(discord);

        jda = new JdaBuilder();

        getLogger().info("\n§d  ___  __    ____    __    _  _    ___   ___ \n" +
                " / __)(  )  ( ___)  /__\\  ( \\( )  / __) / __)\n" +
                "( (__  )(__  )__)  /(__)\\  )  (   \\__ \\( (__ \n" +
                " \\___)(____)(____)(__)(__)(_)\\_)  (___/ \\___)\n");


        loadFiles();

        getLogger().info("§7Configurations loaded §dsuccessfully§7!");


        if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda.startJDA();

            jda.getJda().getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.of(net.dv8tion.jda.api.entities.Activity.ActivityType.valueOf
                            (VelocityDiscordConfig.DISCORD_ACTIVITY_TYPE.get(String.class).toUpperCase()),
                    VelocityDiscordConfig.DISCORD_ACTIVITY.get(String.class)));

            if (getServer().getPluginManager().isLoaded("serverutils")) {

                getLogger().warn("\n§f\n§e§lWARNING!" +
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

            registerStaffChat();

        }

        if (VelocityConfig.DONORCHAT.get(Boolean.class)) {

            registerDonorChat();

        }

        if (VelocityConfig.ADMINCHAT.get(Boolean.class)) {

            registerAdminChat();

        }

        if (VelocityRedis.REDIS_ENABLE.get(Boolean.class) && !getServer().getPluginManager().isLoaded("redisbungee")) {

            getLogger().error("RedisBungee was not found, the RedisBungee hook won't work.");

        }

        if (VelocityRedis.REDIS_ENABLE.get(Boolean.class) && getServer().getPluginManager().isLoaded("redisbungee")) {

            registerRedisBungee();

            getLogger().info("§7Hooked into RedisBungee §dsuccessfully§7!");

        }

        if (VelocityConfig.STATS.get(Boolean.class) && !Version.contains("alpha")) {

            metricsFactory.make(this, 16447);

            getLogger().info("§7Metrics loaded §dsuccessfully§7!");

        }

        getLogger().warn("Some functions are not available on Velocity in 1.19+ clients, this is due to Mojang's self-moderation.");

        if (VelocityConfig.UPDATE_CHECK.get(Boolean.class) && !Version.contains("alpha")) {

            UpdateChecker();

        }

        getLogger().info("§7Plugin successfully §denabled§7!");

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        getLogger().info("Deleting instances...");
        instance = null;
        configTextFile = null;

        logger.info("§7Successfully §ddisabled§7.");
    }

    private void loadFiles() {

        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");
        discordTextFile = new TextFile(path, "discord.yml");
        aliasesTextFile = new TextFile(path, "aliases.yml");
        redisTextFile = new TextFile(path, "redis.yml");

    }

    private void UpdateChecker() {

        new UpdateCheck(this).getVersion(version -> {
            if (container.getDescription().getVersion().isPresent()) {
                if (!container.getDescription().getVersion().get().equals(version)) {
                    getLogger().warn("There is a new update available, download it on SpigotMC!");
                }
            }
        });

    }

    private void registerRedisBungee() {

        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

        server.getEventManager().register(this, new RedisListener(this));

        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-AdminMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-DonorMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffAFKMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffOtherMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-StaffAFKMessage-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-MuteStaffChat-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-MuteAdminChat-RedisBungee");
        redisBungeeAPI.registerPubSubChannels("CleanStaffChat-MuteDonorChat-RedisBungee");

    }

    @SneakyThrows
    private void registerStaffChat() {

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

        if (VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.staffchat.listeners.ChatListener(this));

        }
    }

    @SneakyThrows
    private void registerDonorChat() {

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

        if (VelocityConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {

            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.donorchat.listeners.ChatListener(this));

        }
    }

    @SneakyThrows
    private void registerAdminChat() {

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

            jda.getJda().addEventListener(new it.frafol.cleanstaffchat.velocity.adminchat.listeners.ChatListener(this));

        }
    }
}