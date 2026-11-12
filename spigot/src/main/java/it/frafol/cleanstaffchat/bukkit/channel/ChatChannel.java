package it.frafol.cleanstaffchat.bukkit.channel;

import it.frafol.cleanstaffchat.bukkit.enums.SpigotCommandsConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import org.bukkit.command.CommandSender;

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
                    SpigotConfig.STAFFCHAT, SpigotConfig.STAFFCHAT_USE_PERMISSION, SpigotConfig.STAFFCHAT_TALK_MODULE,
                    SpigotConfig.STAFFCHAT_DISCORD_MODULE, SpigotConfig.STAFFCHAT_PREFIX_MODULE, SpigotConfig.STAFFCHAT_PREFIX,
                    SpigotConfig.STAFFCHAT_TOGGLE_MODULE, SpigotConfig.STAFFCHAT_TOGGLE_PERMISSION,
                    SpigotConfig.STAFFCHAT_MUTE_MODULE, SpigotConfig.STAFFCHAT_MUTE_PERMISSION),
            new Texts(
                    SpigotMessages.PREFIX, SpigotMessages.ARGUMENTS, SpigotMessages.STAFFCHAT_FORMAT,
                    SpigotMessages.STAFFCHAT_CONSOLE_FORMAT, SpigotMessages.STAFFCHAT_FORMAT_DISCORD,
                    SpigotMessages.DISCORD_STAFF_FORMAT, SpigotMessages.STAFFCHAT_MUTED_ERROR,
                    SpigotMessages.STAFFCHAT_MUTED, SpigotMessages.STAFFCHAT_UNMUTED,
                    SpigotMessages.STAFFCHAT_TOGGLED_ON, SpigotMessages.STAFFCHAT_TOGGLED_OFF,
                    SpigotMessages.STAFFCHAT_TALK_ENABLED, SpigotMessages.STAFFCHAT_TALK_DISABLED),
            new Discord(
                    SpigotDiscordConfig.STAFF_CHANNEL_ID, SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE,
                    SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR),
            new Aliases(SpigotCommandsConfig.STAFFCHAT, SpigotCommandsConfig.STAFFCHAT_MUTE, SpigotCommandsConfig.STAFFCHAT_TOGGLE),
            false, false),

    ADMIN(
            new Settings(
                    SpigotConfig.ADMINCHAT, SpigotConfig.ADMINCHAT_USE_PERMISSION, SpigotConfig.ADMINCHAT_TALK_MODULE,
                    SpigotConfig.ADMINCHAT_DISCORD_MODULE, SpigotConfig.ADMINCHAT_PREFIX_MODULE, SpigotConfig.ADMINCHAT_PREFIX,
                    SpigotConfig.ADMINCHAT_TOGGLE_MODULE, SpigotConfig.ADMINCHAT_TOGGLE_PERMISSION,
                    SpigotConfig.ADMINCHAT_MUTE_MODULE, SpigotConfig.ADMINCHAT_MUTE_PERMISSION),
            new Texts(
                    SpigotMessages.ADMINPREFIX, SpigotMessages.ADMINARGUMENTS, SpigotMessages.ADMINCHAT_FORMAT,
                    SpigotMessages.ADMINCHAT_CONSOLE_FORMAT, SpigotMessages.ADMINCHAT_FORMAT_DISCORD,
                    SpigotMessages.DISCORD_ADMIN_FORMAT, SpigotMessages.ADMINCHAT_MUTED_ERROR,
                    SpigotMessages.ADMINCHAT_MUTED, SpigotMessages.ADMINCHAT_UNMUTED,
                    SpigotMessages.ADMINCHAT_TOGGLED_ON, SpigotMessages.ADMINCHAT_TOGGLED_OFF,
                    SpigotMessages.ADMINCHAT_TALK_ENABLED, SpigotMessages.ADMINCHAT_TALK_DISABLED),
            new Discord(
                    SpigotDiscordConfig.ADMIN_CHANNEL_ID, SpigotDiscordConfig.ADMINCHAT_EMBED_TITLE,
                    SpigotDiscordConfig.EMBEDS_ADMINCHATCOLOR),
            new Aliases(SpigotCommandsConfig.ADMINCHAT, SpigotCommandsConfig.ADMINCHAT_MUTE, SpigotCommandsConfig.ADMINCHAT_TOGGLE),
            false, false),

    DONOR(
            new Settings(
                    SpigotConfig.DONORCHAT, SpigotConfig.DONORCHAT_USE_PERMISSION, SpigotConfig.DONORCHAT_TALK_MODULE,
                    SpigotConfig.DONORCHAT_DISCORD_MODULE, SpigotConfig.DONORCHAT_PREFIX_MODULE, SpigotConfig.DONORCHAT_PREFIX,
                    SpigotConfig.DONORCHAT_TOGGLE_MODULE, SpigotConfig.DONORCHAT_TOGGLE_PERMISSION,
                    SpigotConfig.DONORCHAT_MUTE_MODULE, SpigotConfig.DONORCHAT_MUTE_PERMISSION),
            new Texts(
                    SpigotMessages.DONORPREFIX, SpigotMessages.DONORARGUMENTS, SpigotMessages.DONORCHAT_FORMAT,
                    SpigotMessages.DONORCHAT_CONSOLE_FORMAT, SpigotMessages.DONORCHAT_FORMAT_DISCORD,
                    SpigotMessages.DISCORD_DONOR_FORMAT, SpigotMessages.DONORCHAT_MUTED_ERROR,
                    SpigotMessages.DONORCHAT_MUTED, SpigotMessages.DONORCHAT_UNMUTED,
                    SpigotMessages.DONORCHAT_TOGGLED_ON, SpigotMessages.DONORCHAT_TOGGLED_OFF,
                    SpigotMessages.DONORCHAT_TALK_ENABLED, SpigotMessages.DONORCHAT_TALK_DISABLED),
            new Discord(
                    SpigotDiscordConfig.DONOR_CHANNEL_ID, SpigotDiscordConfig.DONORCHAT_EMBED_TITLE,
                    SpigotDiscordConfig.EMBEDS_DONORCHATCOLOR),
            new Aliases(SpigotCommandsConfig.DONORCHAT, SpigotCommandsConfig.DONORCHAT_MUTE, SpigotCommandsConfig.DONORCHAT_TOGGLE),
            true, true);

    /** Config switches and permissions. */
    public static final class Settings {
        public final SpigotConfig enabled;
        public final SpigotConfig usePermission;
        public final SpigotConfig talkModule;
        public final SpigotConfig discordModule;
        public final SpigotConfig prefixModule;
        public final SpigotConfig prefixSymbol;
        public final SpigotConfig toggleModule;
        public final SpigotConfig togglePermission;
        public final SpigotConfig muteModule;
        public final SpigotConfig mutePermission;

        Settings(SpigotConfig enabled, SpigotConfig usePermission, SpigotConfig talkModule, SpigotConfig discordModule,
                 SpigotConfig prefixModule, SpigotConfig prefixSymbol, SpigotConfig toggleModule,
                 SpigotConfig togglePermission, SpigotConfig muteModule, SpigotConfig mutePermission) {
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
        }
    }

    /** Messages (messages.yml). */
    public static final class Texts {
        public final SpigotMessages prefix;
        public final SpigotMessages arguments;
        public final SpigotMessages format;
        public final SpigotMessages consoleFormat;
        public final SpigotMessages discordFormat;
        public final SpigotMessages fromDiscordFormat;
        public final SpigotMessages mutedError;
        public final SpigotMessages muted;
        public final SpigotMessages unmuted;
        public final SpigotMessages toggledOn;
        public final SpigotMessages toggledOff;
        public final SpigotMessages talkEnabled;
        public final SpigotMessages talkDisabled;

        Texts(SpigotMessages prefix, SpigotMessages arguments, SpigotMessages format, SpigotMessages consoleFormat,
              SpigotMessages discordFormat, SpigotMessages fromDiscordFormat, SpigotMessages mutedError,
              SpigotMessages muted, SpigotMessages unmuted, SpigotMessages toggledOn, SpigotMessages toggledOff,
              SpigotMessages talkEnabled, SpigotMessages talkDisabled) {
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
        public final SpigotDiscordConfig channelId;
        public final SpigotDiscordConfig embedTitle;
        public final SpigotDiscordConfig embedColor;

        Discord(SpigotDiscordConfig channelId, SpigotDiscordConfig embedTitle, SpigotDiscordConfig embedColor) {
            this.channelId = channelId;
            this.embedTitle = embedTitle;
            this.embedColor = embedColor;
        }
    }

    /** Command aliases (aliases.yml). */
    public static final class Aliases {
        public final SpigotCommandsConfig main;
        public final SpigotCommandsConfig mute;
        public final SpigotCommandsConfig toggle;

        Aliases(SpigotCommandsConfig main, SpigotCommandsConfig mute, SpigotCommandsConfig toggle) {
            this.main = main;
            this.mute = mute;
            this.toggle = toggle;
        }
    }

    public final Settings settings;
    public final Texts texts;
    public final Discord discord;
    public final Aliases aliases;

    /** Players on whom the external punishment plugin (LiteBans / LibertyBans) mute applies. */
    private final boolean respectsPunishmentMute;
    /** Whether the channel has the per-player cooldown (donor chat). */
    private final boolean cooldown;

    /** Players that switched the channel off (they do not receive its messages). */
    private final Set<UUID> hidden = ConcurrentHashMap.newKeySet();
    /** Players whose normal chat messages are redirected to this channel. */
    private final Set<UUID> talking = ConcurrentHashMap.newKeySet();
    private volatile boolean muted;

    ChatChannel(Settings settings, Texts texts, Discord discord, Aliases aliases,
                boolean respectsPunishmentMute, boolean cooldown) {
        this.settings = settings;
        this.texts = texts;
        this.discord = discord;
        this.aliases = aliases;
        this.respectsPunishmentMute = respectsPunishmentMute;
        this.cooldown = cooldown;
    }

    public boolean isEnabled() {
        return settings.enabled.get(Boolean.class);
    }

    public boolean respectsPunishmentMute() {
        return respectsPunishmentMute;
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

    /** Sends a configured message to the target, resolving %prefix% with this channel's prefix. */
    public void reply(CommandSender target, SpigotMessages message) {
        target.sendMessage(message.color().replace("%prefix%", prefix()));
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
}
