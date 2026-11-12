package it.frafol.cleanstaffchat.bungee.channel;

import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.enums.BungeeServers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One chat channel (staff, admin, donor). Everything that differs between the channels lives here,
 * so listeners and commands are written once and parameterised with a channel.
 * <p>
 * To add a new channel: add a constant below and register it in CleanStaffChat#registerChannel.
 */
public enum ChatChannel {

    STAFF(
            new Settings(
                    BungeeConfig.STAFFCHAT, BungeeConfig.STAFFCHAT_USE_PERMISSION, BungeeConfig.STAFFCHAT_TALK_MODULE,
                    BungeeConfig.STAFFCHAT_DISCORD_MODULE, BungeeConfig.STAFFCHAT_PREFIX_MODULE, BungeeConfig.STAFFCHAT_PREFIX,
                    BungeeConfig.STAFFCHAT_TOGGLE_MODULE, BungeeConfig.STAFFCHAT_TOGGLE_PERMISSION,
                    BungeeConfig.STAFFCHAT_MUTE_MODULE, BungeeConfig.STAFFCHAT_MUTE_PERMISSION,
                    BungeeServers.STAFFCHAT_ENABLE, BungeeServers.SC_BLOCKED_SRV),
            new Texts(
                    BungeeMessages.PREFIX, BungeeMessages.ARGUMENTS, BungeeMessages.STAFFCHAT_FORMAT,
                    BungeeMessages.STAFFCHAT_CONSOLE_FORMAT, BungeeMessages.STAFFCHAT_FORMAT_DISCORD,
                    BungeeMessages.DISCORD_STAFF_FORMAT, BungeeMessages.STAFFCHAT_MUTED_ERROR,
                    BungeeMessages.STAFFCHAT_MUTED, BungeeMessages.STAFFCHAT_UNMUTED,
                    BungeeMessages.STAFFCHAT_TOGGLED_ON, BungeeMessages.STAFFCHAT_TOGGLED_OFF,
                    BungeeMessages.STAFFCHAT_TALK_ENABLED, BungeeMessages.STAFFCHAT_TALK_DISABLED),
            new Discord(
                    BungeeDiscordConfig.STAFF_CHANNEL_ID, BungeeDiscordConfig.STAFFCHAT_EMBED_TITLE,
                    BungeeDiscordConfig.EMBEDS_STAFFCHATCOLOR),
            new Aliases(BungeeCommandsConfig.STAFFCHAT, BungeeCommandsConfig.STAFFCHAT_MUTE, BungeeCommandsConfig.STAFFCHAT_TOGGLE),
            "CleanStaffChat-StaffMessage-RedisBungee", "CleanStaffChat-MuteStaffChat-RedisBungee",
            false),

    ADMIN(
            new Settings(
                    BungeeConfig.ADMINCHAT, BungeeConfig.ADMINCHAT_USE_PERMISSION, BungeeConfig.ADMINCHAT_TALK_MODULE,
                    BungeeConfig.ADMINCHAT_DISCORD_MODULE, BungeeConfig.ADMINCHAT_PREFIX_MODULE, BungeeConfig.ADMINCHAT_PREFIX,
                    BungeeConfig.ADMINCHAT_TOGGLE_MODULE, BungeeConfig.ADMINCHAT_TOGGLE_PERMISSION,
                    BungeeConfig.ADMINCHAT_MUTE_MODULE, BungeeConfig.ADMINCHAT_MUTE_PERMISSION,
                    BungeeServers.ADMINCHAT_ENABLE, BungeeServers.AC_BLOCKED_SRV),
            new Texts(
                    BungeeMessages.ADMINPREFIX, BungeeMessages.ADMINARGUMENTS, BungeeMessages.ADMINCHAT_FORMAT,
                    BungeeMessages.ADMINCHAT_CONSOLE_FORMAT, BungeeMessages.ADMINCHAT_FORMAT_DISCORD,
                    BungeeMessages.DISCORD_ADMIN_FORMAT, BungeeMessages.ADMINCHAT_MUTED_ERROR,
                    BungeeMessages.ADMINCHAT_MUTED, BungeeMessages.ADMINCHAT_UNMUTED,
                    BungeeMessages.ADMINCHAT_TOGGLED_ON, BungeeMessages.ADMINCHAT_TOGGLED_OFF,
                    BungeeMessages.ADMINCHAT_TALK_ENABLED, BungeeMessages.ADMINCHAT_TALK_DISABLED),
            new Discord(
                    BungeeDiscordConfig.ADMIN_CHANNEL_ID, BungeeDiscordConfig.ADMINCHAT_EMBED_TITLE,
                    BungeeDiscordConfig.EMBEDS_ADMINCHATCOLOR),
            new Aliases(BungeeCommandsConfig.ADMINCHAT, BungeeCommandsConfig.ADMINCHAT_MUTE, BungeeCommandsConfig.ADMINCHAT_TOGGLE),
            "CleanStaffChat-AdminMessage-RedisBungee", "CleanStaffChat-MuteAdminChat-RedisBungee",
            false),

    DONOR(
            new Settings(
                    BungeeConfig.DONORCHAT, BungeeConfig.DONORCHAT_USE_PERMISSION, BungeeConfig.DONORCHAT_TALK_MODULE,
                    BungeeConfig.DONORCHAT_DISCORD_MODULE, BungeeConfig.DONORCHAT_PREFIX_MODULE, BungeeConfig.DONORCHAT_PREFIX,
                    BungeeConfig.DONORCHAT_TOGGLE_MODULE, BungeeConfig.DONORCHAT_TOGGLE_PERMISSION,
                    BungeeConfig.DONORCHAT_MUTE_MODULE, BungeeConfig.DONORCHAT_MUTE_PERMISSION,
                    BungeeServers.DONORCHAT_ENABLE, BungeeServers.DC_BLOCKED_SRV),
            new Texts(
                    BungeeMessages.DONORPREFIX, BungeeMessages.DONORARGUMENTS, BungeeMessages.DONORCHAT_FORMAT,
                    BungeeMessages.DONORCHAT_CONSOLE_FORMAT, BungeeMessages.DONORCHAT_FORMAT_DISCORD,
                    BungeeMessages.DISCORD_DONOR_FORMAT, BungeeMessages.DONORCHAT_MUTED_ERROR,
                    BungeeMessages.DONORCHAT_MUTED, BungeeMessages.DONORCHAT_UNMUTED,
                    BungeeMessages.DONORCHAT_TOGGLED_ON, BungeeMessages.DONORCHAT_TOGGLED_OFF,
                    BungeeMessages.DONORCHAT_TALK_ENABLED, BungeeMessages.DONORCHAT_TALK_DISABLED),
            new Discord(
                    BungeeDiscordConfig.DONOR_CHANNEL_ID, BungeeDiscordConfig.DONORCHAT_EMBED_TITLE,
                    BungeeDiscordConfig.EMBEDS_DONORCHATCOLOR),
            new Aliases(BungeeCommandsConfig.DONORCHAT, BungeeCommandsConfig.DONORCHAT_MUTE, BungeeCommandsConfig.DONORCHAT_TOGGLE),
            "CleanStaffChat-DonorMessage-RedisBungee", "CleanStaffChat-MuteDonorChat-RedisBungee",
            true);

    /** Config switches and permissions. */
    public static final class Settings {
        public final BungeeConfig enabled;
        public final BungeeConfig usePermission;
        public final BungeeConfig talkModule;
        public final BungeeConfig discordModule;
        public final BungeeConfig prefixModule;
        public final BungeeConfig prefixSymbol;
        public final BungeeConfig toggleModule;
        public final BungeeConfig togglePermission;
        public final BungeeConfig muteModule;
        public final BungeeConfig mutePermission;
        public final BungeeServers serversEnabled;
        public final BungeeServers blockedServers;

        Settings(BungeeConfig enabled, BungeeConfig usePermission, BungeeConfig talkModule, BungeeConfig discordModule,
                 BungeeConfig prefixModule, BungeeConfig prefixSymbol, BungeeConfig toggleModule,
                 BungeeConfig togglePermission, BungeeConfig muteModule, BungeeConfig mutePermission,
                 BungeeServers serversEnabled, BungeeServers blockedServers) {
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
        public final BungeeMessages prefix;
        public final BungeeMessages arguments;
        public final BungeeMessages format;
        public final BungeeMessages consoleFormat;
        public final BungeeMessages discordFormat;
        public final BungeeMessages fromDiscordFormat;
        public final BungeeMessages mutedError;
        public final BungeeMessages muted;
        public final BungeeMessages unmuted;
        public final BungeeMessages toggledOn;
        public final BungeeMessages toggledOff;
        public final BungeeMessages talkEnabled;
        public final BungeeMessages talkDisabled;

        Texts(BungeeMessages prefix, BungeeMessages arguments, BungeeMessages format, BungeeMessages consoleFormat,
              BungeeMessages discordFormat, BungeeMessages fromDiscordFormat, BungeeMessages mutedError,
              BungeeMessages muted, BungeeMessages unmuted, BungeeMessages toggledOn, BungeeMessages toggledOff,
              BungeeMessages talkEnabled, BungeeMessages talkDisabled) {
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
        public final BungeeDiscordConfig channelId;
        public final BungeeDiscordConfig embedTitle;
        public final BungeeDiscordConfig embedColor;

        Discord(BungeeDiscordConfig channelId, BungeeDiscordConfig embedTitle, BungeeDiscordConfig embedColor) {
            this.channelId = channelId;
            this.embedTitle = embedTitle;
            this.embedColor = embedColor;
        }
    }

    /** Command aliases (aliases.yml). */
    public static final class Aliases {
        public final BungeeCommandsConfig main;
        public final BungeeCommandsConfig mute;
        public final BungeeCommandsConfig toggle;

        Aliases(BungeeCommandsConfig main, BungeeCommandsConfig mute, BungeeCommandsConfig toggle) {
            this.main = main;
            this.mute = mute;
            this.toggle = toggle;
        }
    }

    public final Settings settings;
    public final Texts texts;
    public final Discord discord;
    public final Aliases aliases;
    /** RedisBungee pub/sub channel carrying this channel's chat messages. */
    public final String redisMessageChannel;
    /** RedisBungee pub/sub channel used to synchronise the mute state. */
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

    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(settings.usePermission.get(String.class));
    }

    /** True when the player is on a server where this channel is disabled (servers.yml). */
    public boolean isBlocked(ProxiedPlayer player) {
        if (!settings.serversEnabled.get(Boolean.class) || player.getServer() == null) return false;
        final String server = player.getServer().getInfo().getName();
        final List<String> blocked = settings.blockedServers.getStringList();
        for (String name : blocked) {
            if (name.equalsIgnoreCase(server)) return true;
        }
        return false;
    }

    /** Sends a configured message to the target, resolving %prefix% with this channel's prefix. */
    public void reply(CommandSender target, BungeeMessages message) {
        target.sendMessage(TextComponent.fromLegacy(message.color().replace("%prefix%", prefix())));
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
