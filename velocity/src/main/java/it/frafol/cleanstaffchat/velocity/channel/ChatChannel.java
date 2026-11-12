package it.frafol.cleanstaffchat.velocity.channel;

import it.frafol.cleanstaffchat.velocity.enums.VelocityCommandsConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityDiscordConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.enums.VelocityServers;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One chat channel (staff, admin, donor). Everything that differs between the channels lives here,
 * so listeners and commands are written once and parameterized with a channel.
 * <p>
 * To add a new channel: add a constant below and register it in CleanStaffChat#registerChannel.
 */
public enum ChatChannel {

    STAFF(
            new Settings(
                    VelocityConfig.STAFFCHAT, VelocityConfig.STAFFCHAT_USE_PERMISSION, VelocityConfig.STAFFCHAT_TALK_MODULE,
                    VelocityConfig.STAFFCHAT_DISCORD_MODULE, VelocityConfig.STAFFCHAT_PREFIX_MODULE, VelocityConfig.STAFFCHAT_PREFIX,
                    VelocityConfig.STAFFCHAT_TOGGLE_MODULE, VelocityConfig.STAFFCHAT_TOGGLE_PERMISSION,
                    VelocityConfig.STAFFCHAT_MUTE_MODULE, VelocityConfig.STAFFCHAT_MUTE_PERMISSION,
                    VelocityServers.STAFFCHAT_ENABLE, VelocityServers.SC_BLOCKED_SRV),
            new Texts(
                    VelocityMessages.PREFIX, VelocityMessages.ARGUMENTS, VelocityMessages.STAFFCHAT_FORMAT,
                    VelocityMessages.STAFFCHAT_CONSOLE_FORMAT, VelocityMessages.STAFFCHAT_FORMAT_DISCORD,
                    VelocityMessages.DISCORD_STAFF_FORMAT, VelocityMessages.STAFFCHAT_MUTED_ERROR,
                    VelocityMessages.STAFFCHAT_MUTED, VelocityMessages.STAFFCHAT_UNMUTED,
                    VelocityMessages.STAFFCHAT_TOGGLED_ON, VelocityMessages.STAFFCHAT_TOGGLED_OFF,
                    VelocityMessages.STAFFCHAT_TALK_ENABLED, VelocityMessages.STAFFCHAT_TALK_DISABLED),
            new Discord(
                    VelocityDiscordConfig.STAFF_CHANNEL_ID, VelocityDiscordConfig.STAFFCHAT_EMBED_TITLE,
                    VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR),
            new Aliases(VelocityCommandsConfig.STAFFCHAT, VelocityCommandsConfig.STAFFCHAT_MUTE, VelocityCommandsConfig.STAFFCHAT_TOGGLE),
            "CleanStaffChat-StaffMessage-RedisVelocity", "CleanStaffChat-MuteStaffChat-RedisVelocity",
            false),

    ADMIN(
            new Settings(
                    VelocityConfig.ADMINCHAT, VelocityConfig.ADMINCHAT_USE_PERMISSION, VelocityConfig.ADMINCHAT_TALK_MODULE,
                    VelocityConfig.ADMINCHAT_DISCORD_MODULE, VelocityConfig.ADMINCHAT_PREFIX_MODULE, VelocityConfig.ADMINCHAT_PREFIX,
                    VelocityConfig.ADMINCHAT_TOGGLE_MODULE, VelocityConfig.ADMINCHAT_TOGGLE_PERMISSION,
                    VelocityConfig.ADMINCHAT_MUTE_MODULE, VelocityConfig.ADMINCHAT_MUTE_PERMISSION,
                    VelocityServers.ADMINCHAT_ENABLE, VelocityServers.AC_BLOCKED_SRV),
            new Texts(
                    VelocityMessages.ADMINPREFIX, VelocityMessages.ADMINARGUMENTS, VelocityMessages.ADMINCHAT_FORMAT,
                    VelocityMessages.ADMINCHAT_CONSOLE_FORMAT, VelocityMessages.ADMINCHAT_FORMAT_DISCORD,
                    VelocityMessages.DISCORD_ADMIN_FORMAT, VelocityMessages.ADMINCHAT_MUTED_ERROR,
                    VelocityMessages.ADMINCHAT_MUTED, VelocityMessages.ADMINCHAT_UNMUTED,
                    VelocityMessages.ADMINCHAT_TOGGLED_ON, VelocityMessages.ADMINCHAT_TOGGLED_OFF,
                    VelocityMessages.ADMINCHAT_TALK_ENABLED, VelocityMessages.ADMINCHAT_TALK_DISABLED),
            new Discord(
                    VelocityDiscordConfig.ADMIN_CHANNEL_ID, VelocityDiscordConfig.ADMINCHAT_EMBED_TITLE,
                    VelocityDiscordConfig.EMBEDS_ADMINCHATCOLOR),
            new Aliases(VelocityCommandsConfig.ADMINCHAT, VelocityCommandsConfig.ADMINCHAT_MUTE, VelocityCommandsConfig.ADMINCHAT_TOGGLE),
            "CleanStaffChat-AdminMessage-RedisVelocity", "CleanStaffChat-MuteAdminChat-RedisVelocity",
            false),

    DONOR(
            new Settings(
                    VelocityConfig.DONORCHAT, VelocityConfig.DONORCHAT_USE_PERMISSION, VelocityConfig.DONORCHAT_TALK_MODULE,
                    VelocityConfig.DONORCHAT_DISCORD_MODULE, VelocityConfig.DONORCHAT_PREFIX_MODULE, VelocityConfig.DONORCHAT_PREFIX,
                    VelocityConfig.DONORCHAT_TOGGLE_MODULE, VelocityConfig.DONORCHAT_TOGGLE_PERMISSION,
                    VelocityConfig.DONORCHAT_MUTE_MODULE, VelocityConfig.DONORCHAT_MUTE_PERMISSION,
                    VelocityServers.DONORCHAT_ENABLE, VelocityServers.DC_BLOCKED_SRV),
            new Texts(
                    VelocityMessages.DONORPREFIX, VelocityMessages.DONORARGUMENTS, VelocityMessages.DONORCHAT_FORMAT,
                    VelocityMessages.DONORCHAT_CONSOLE_FORMAT, VelocityMessages.DONORCHAT_FORMAT_DISCORD,
                    VelocityMessages.DISCORD_DONOR_FORMAT, VelocityMessages.DONORCHAT_MUTED_ERROR,
                    VelocityMessages.DONORCHAT_MUTED, VelocityMessages.DONORCHAT_UNMUTED,
                    VelocityMessages.DONORCHAT_TOGGLED_ON, VelocityMessages.DONORCHAT_TOGGLED_OFF,
                    VelocityMessages.DONORCHAT_TALK_ENABLED, VelocityMessages.DONORCHAT_TALK_DISABLED),
            new Discord(
                    VelocityDiscordConfig.DONOR_CHANNEL_ID, VelocityDiscordConfig.DONORCHAT_EMBED_TITLE,
                    VelocityDiscordConfig.EMBEDS_DONORCHATCOLOR),
            new Aliases(VelocityCommandsConfig.DONORCHAT, VelocityCommandsConfig.DONORCHAT_MUTE, VelocityCommandsConfig.DONORCHAT_TOGGLE),
            "CleanStaffChat-DonorMessage-RedisVelocity", "CleanStaffChat-MuteDonorChat-RedisVelocity",
            true);

    /** Config switches and permissions. */
    public static final class Settings {
        public final VelocityConfig enabled;
        public final VelocityConfig usePermission;
        public final VelocityConfig talkModule;
        public final VelocityConfig discordModule;
        public final VelocityConfig prefixModule;
        public final VelocityConfig prefixSymbol;
        public final VelocityConfig toggleModule;
        public final VelocityConfig togglePermission;
        public final VelocityConfig muteModule;
        public final VelocityConfig mutePermission;
        public final VelocityServers serversEnabled;
        public final VelocityServers blockedServers;

        Settings(VelocityConfig enabled, VelocityConfig usePermission, VelocityConfig talkModule, VelocityConfig discordModule,
                 VelocityConfig prefixModule, VelocityConfig prefixSymbol, VelocityConfig toggleModule,
                 VelocityConfig togglePermission, VelocityConfig muteModule, VelocityConfig mutePermission,
                 VelocityServers serversEnabled, VelocityServers blockedServers) {
            this.enabled = enabled;
            this.usePermission = usePermission;
            this.talkModule = talkModule;
            this.discordModule = discordModule;
            this.prefixModule = prefixModule;
            this.prefixSymbol = prefixSymbol;
            this.toggleModule = toggleModule;
            this.togglePermission = togglePermission;
            this.muteModule = muteModule;
            this.mutePermission = mutePermission;
            this.serversEnabled = serversEnabled;
            this.blockedServers = blockedServers;
        }
    }

    /** Messages (messages.yml). */
    public static final class Texts {
        public final VelocityMessages prefix;
        public final VelocityMessages arguments;
        public final VelocityMessages format;
        public final VelocityMessages consoleFormat;
        public final VelocityMessages discordFormat;
        public final VelocityMessages fromDiscordFormat;
        public final VelocityMessages mutedError;
        public final VelocityMessages muted;
        public final VelocityMessages unmuted;
        public final VelocityMessages toggledOn;
        public final VelocityMessages toggledOff;
        public final VelocityMessages talkEnabled;
        public final VelocityMessages talkDisabled;

        Texts(VelocityMessages prefix, VelocityMessages arguments, VelocityMessages format, VelocityMessages consoleFormat,
              VelocityMessages discordFormat, VelocityMessages fromDiscordFormat, VelocityMessages mutedError,
              VelocityMessages muted, VelocityMessages unmuted, VelocityMessages toggledOn, VelocityMessages toggledOff,
              VelocityMessages talkEnabled, VelocityMessages talkDisabled) {
            this.prefix = prefix;
            this.arguments = arguments;
            this.format = format;
            this.consoleFormat = consoleFormat;
            this.discordFormat = discordFormat;
            this.fromDiscordFormat = fromDiscordFormat;
            this.mutedError = mutedError;
            this.muted = muted;
            this.unmuted = unmuted;
            this.toggledOn = toggledOn;
            this.toggledOff = toggledOff;
            this.talkEnabled = talkEnabled;
            this.talkDisabled = talkDisabled;
        }
    }

    /** Discord settings (discord.yml). */
    public static final class Discord {
        public final VelocityDiscordConfig channelId;
        public final VelocityDiscordConfig embedTitle;
        public final VelocityDiscordConfig embedColor;

        Discord(VelocityDiscordConfig channelId, VelocityDiscordConfig embedTitle, VelocityDiscordConfig embedColor) {
            this.channelId = channelId;
            this.embedTitle = embedTitle;
            this.embedColor = embedColor;
        }
    }

    /** Command aliases (aliases.yml). */
    public static final class Aliases {
        public final VelocityCommandsConfig main;
        public final VelocityCommandsConfig mute;
        public final VelocityCommandsConfig toggle;

        Aliases(VelocityCommandsConfig main, VelocityCommandsConfig mute, VelocityCommandsConfig toggle) {
            this.main = main;
            this.mute = mute;
            this.toggle = toggle;
        }
    }

    public final Settings settings;
    public final Texts texts;
    public final Discord discord;
    public final Aliases aliases;
    /** RedisVelocity pub/sub channel carrying this channel's chat messages. */
    public final String redisMessageChannel;
    /** RedisVelocity pub/sub channel used to synchronise the mute state. */
    public final String redisMuteChannel;

    /** Whether the channel has the per-player cooldown (donor chat). */
    private final boolean cooldown;

    /** Players that switched the channel off (they do not receive its messages). */
    private final Set<UUID> hidden = ConcurrentHashMap.newKeySet();
    /** Players whose normal chat messages are redirected to this channel. */
    private final Set<UUID> talking = ConcurrentHashMap.newKeySet();
    private volatile boolean muted;

    ChatChannel(Settings settings, Texts texts, Discord discord, Aliases aliases,
                String redisMessageChannel, String redisMuteChannel, boolean cooldown) {
        this.settings = settings;
        this.texts = texts;
        this.discord = discord;
        this.aliases = aliases;
        this.redisMessageChannel = redisMessageChannel;
        this.redisMuteChannel = redisMuteChannel;
        this.cooldown = cooldown;
    }

    public boolean isEnabled() {
        return settings.enabled.get(Boolean.class);
    }

    public boolean hasCooldown() {
        return cooldown;
    }

    public String prefix() {
        return texts.prefix.color();
    }

    public boolean canUse(CommandSource sender) {
        return sender.hasPermission(settings.usePermission.get(String.class));
    }

    /** True when the player is on a server where this channel is disabled (servers.yml). */
    public boolean isBlocked(Player player) {
        if (!settings.serversEnabled.get(Boolean.class) || player.getCurrentServer().isEmpty()) return false;
        final String server = player.getCurrentServer().get().getServerInfo().getName();
        final List<String> blocked = settings.blockedServers.getStringList();
        for (String name : blocked) {
            if (name.equalsIgnoreCase(server)) return true;
        }
        return false;
    }

    /** Sends a configured message to the target, resolving %prefix% with this channel's prefix. */
    public void reply(CommandSource target, VelocityMessages message) {
        message.send(target, new Placeholder("prefix", prefix()));
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isHidden(UUID uuid) {
        return hidden.contains(uuid);
    }

    public void setHidden(UUID uuid, boolean value) {
        if (value) hidden.add(uuid); else hidden.remove(uuid);
    }

    public boolean isTalking(UUID uuid) {
        return talking.contains(uuid);
    }

    public void setTalking(UUID uuid, boolean value) {
        if (value) talking.add(uuid); else talking.remove(uuid);
    }

    /** A player talks in one channel at a time: starting one stops the others. */
    public void startTalking(UUID uuid) {
        for (ChatChannel channel : values()) channel.talking.remove(uuid);
        talking.add(uuid);
    }

    public static boolean isTalkingAnywhere(UUID uuid) {
        for (ChatChannel channel : values()) {
            if (channel.talking.contains(uuid)) return true;
        }
        return false;
    }
}
