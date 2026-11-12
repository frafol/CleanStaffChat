package it.frafol.cleanstaffchat.velocity.channel;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.enums.VelocityRedis;
import it.frafol.cleanstaffchat.velocity.objects.AffixResolver;
import it.frafol.cleanstaffchat.velocity.objects.Affixes;
import it.frafol.cleanstaffchat.velocity.objects.DiscordBridge;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Delivers a message to a channel: players (locally or through RedisBungee) and Discord.
 * Used by commands and chat listeners so the rules exist only once.
 */
public final class ChannelMessenger {

    private ChannelMessenger() {
    }

    public static boolean isRedisEnabled() {
        return VelocityRedis.REDIS_ENABLE.get(Boolean.class)
                && CleanStaffChat.getInstance().getServer().getPluginManager().isLoaded("redisbungee");
    }

    public static void publish(String redisChannel, String payload) {
        RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(redisChannel, payload);
    }

    /** Converts a line received through Redis (legacy section colors) into a component. */
    public static Component legacy(String line) {
        return LegacyComponentSerializer.legacy('§').deserialize(line);
    }

    /**
     * Validates and delivers a message written by a player.
     *
     * @return false when the message was rejected
     */
    public static boolean deliver(CleanStaffChat plugin, ChatChannel channel, Player player, String message) {
        if (channel.isMuted()) {
            channel.reply(player, channel.texts.mutedError);
            return false;
        }

        if (VelocityConfig.PREVENT_COLOR_CODES.get(Boolean.class) && ChatUtil.hasColorCodes(message)) {
            channel.reply(player, VelocityMessages.COLOR_CODES);
            return false;
        }

        if (channel.hasCooldown() && PlayerCache.getCooldown().contains(player.getUniqueId())) {
            channel.reply(player, VelocityMessages.DONORCHAT_COOLDOWN_MESSAGE);
            return false;
        }

        final Affixes affixes = AffixResolver.resolve(player);
        if (affixes == null) return false;

        final String server = player.getCurrentServer().isPresent()
                ? player.getCurrentServer().get().getServerInfo().getName() : "";
        dispatch(channel, player.getUsername(), server, channel.texts.format, affixes, message);

        if (channel.hasCooldown()) applyCooldown(plugin, player);
        return true;
    }

    /** Delivers a message written from the console and echoes it back to the console. */
    public static void deliverFromConsole(ChatChannel channel, CommandSource console, String message) {
        final String name = VelocityConfig.CONSOLE_PREFIX.get(String.class);
        dispatch(channel, name, "", channel.texts.consoleFormat, Affixes.NONE, message);
        if (!isRedisEnabled()) channel.texts.consoleFormat.send(console, placeholders(channel, name, "", Affixes.NONE, message));
    }

    private static Placeholder[] placeholders(ChatChannel channel, String name, String server, Affixes affixes, String message) {
        return new Placeholder[]{
                new Placeholder("user", name),
                new Placeholder("message", message),
                new Placeholder("displayname", affixes.displayName(name)),
                new Placeholder("userprefix", affixes.coloredPrefix()),
                new Placeholder("usersuffix", affixes.coloredSuffix()),
                new Placeholder("server", server),
                new Placeholder("prefix", channel.prefix())
        };
    }

    private static void dispatch(ChatChannel channel, String senderName, String server, VelocityMessages format, Affixes affixes, String message) {
        if (isRedisEnabled()) {
            final String line = format.get(String.class)
                    .replace("%user%", senderName)
                    .replace("%displayname%", affixes.displayName(senderName))
                    .replace("%userprefix%", affixes.coloredPrefix())
                    .replace("%usersuffix%", affixes.coloredSuffix())
                    .replace("%server%", server)
                    .replace("%prefix%", channel.prefix())
                    .replace("%message%", message)
                    .replace("&", "§");
            publish(channel.redisMessageChannel, line);
        } else broadcastLocal(channel, format, placeholders(channel, senderName, server, affixes, message));
        if (DiscordBridge.isActive(channel.settings.discordModule)) {
            final String discordText = channel.texts.discordFormat.get(String.class)
                    .replace("%user%", senderName)
                    .replace("%userprefix%", Placeholder.stripToDiscord(affixes.prefix()))
                    .replace("%usersuffix%", Placeholder.stripToDiscord(affixes.suffix()))
                    .replace("%message%", message)
                    .replace("%server%", server);
            DiscordBridge.send(channel.discord.channelId, channel.discord.embedTitle, channel.discord.embedColor, discordText);
        }
    }

    /** Sends a configured message to everyone who may read the channel on this proxy. */
    public static void broadcastLocal(ChatChannel channel, VelocityMessages format, Placeholder... placeholders) {
        final String permission = channel.settings.usePermission.get(String.class);
        for (Player recipient : CleanStaffChat.getInstance().getServer().getAllPlayers()) {
            if (recipient.hasPermission(permission)
                    && !channel.isHidden(recipient.getUniqueId())
                    && !channel.isBlocked(recipient)) {
                format.send(recipient, placeholders);
            }
        }
    }

    /** Delivers a line received through Redis: server restrictions were already applied by the sender's proxy. */
    public static void broadcastRedisLine(ChatChannel channel, String line) {
        final String permission = channel.settings.usePermission.get(String.class);
        final Component component = legacy(line);
        for (Player recipient : CleanStaffChat.getInstance().getServer().getAllPlayers()) {
            if (recipient.hasPermission(permission) && !channel.isHidden(recipient.getUniqueId())) {
                recipient.sendMessage(component);
            }
        }
    }

    private static void applyCooldown(CleanStaffChat plugin, Player player) {
        if (player.hasPermission(VelocityConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) return;
        final UUID uuid = player.getUniqueId();
        PlayerCache.getCooldown().add(uuid);
        plugin.getServer().getScheduler()
                .buildTask(plugin, () -> PlayerCache.getCooldown().remove(uuid))
                .delay(VelocityConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS)
                .schedule();
    }
}
