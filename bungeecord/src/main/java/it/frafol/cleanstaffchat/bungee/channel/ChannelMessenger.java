package it.frafol.cleanstaffchat.bungee.channel;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.enums.BungeeRedis;
import it.frafol.cleanstaffchat.bungee.objects.AffixResolver;
import it.frafol.cleanstaffchat.bungee.objects.Affixes;
import it.frafol.cleanstaffchat.bungee.objects.DiscordBridge;
import it.frafol.cleanstaffchat.bungee.objects.Placeholder;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
        return BungeeRedis.REDIS_ENABLE.get(Boolean.class)
                && ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null;
    }

    public static void publish(String redisChannel, String payload) {
        RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(redisChannel, payload);
    }

    /**
     * Validates and delivers a message written by a player.
     *
     * @return false when the message was rejected
     */
    public static boolean deliver(CleanStaffChat plugin, ChatChannel channel, ProxiedPlayer player, String message) {
        if (channel.isMuted()) {
            channel.reply(player, channel.texts.mutedError);
            return false;
        }

        if (BungeeConfig.PREVENT_COLOR_CODES.get(Boolean.class) && PlayerCache.hasColorCodes(message)) {
            player.sendMessage(TextComponent.fromLegacy(BungeeMessages.COLOR_CODES.color()
                    .replace("%prefix%", channel.prefix())
                    .replace("&", "§")));
            return false;
        }

        if (channel.hasCooldown() && PlayerCache.getCooldown().contains(player.getUniqueId())) {
            channel.reply(player, BungeeMessages.DONORCHAT_COOLDOWN_MESSAGE);
            return false;
        }

        final Affixes affixes = AffixResolver.resolve(player);
        if (affixes == null) return false;

        final String server = player.getServer() == null ? "" : player.getServer().getInfo().getName();
        dispatch(channel, player.getName(), server, channel.texts.format.color(), affixes, message);

        if (channel.hasCooldown()) applyCooldown(plugin, player);
        return true;
    }

    /** Delivers a message written from the console. */
    public static void deliverFromConsole(ChatChannel channel, CommandSender console, String message) {
        final String line = dispatch(channel, BungeeConfig.CONSOLE_PREFIX.get(String.class), "",
                channel.texts.consoleFormat.color(), Affixes.NONE, message);
        if (!isRedisEnabled()) console.sendMessage(TextComponent.fromLegacy(line));
    }

    private static String dispatch(ChatChannel channel, String senderName, String server,
                                   String template, Affixes affixes, String message) {

        final String line = template
                .replace("%prefix%", channel.prefix())
                .replace("%user%", senderName)
                .replace("%displayname%", affixes.displayName(senderName))
                .replace("%userprefix%", affixes.coloredPrefix())
                .replace("%usersuffix%", affixes.coloredSuffix())
                .replace("%server%", server)
                .replace("%message%", message)
                .replace("&", "§");

        if (isRedisEnabled()) publish(channel.redisMessageChannel, line);
        else broadcastLocal(channel, line);
        if (DiscordBridge.isActive(channel.settings.discordModule)) {
            final String discordText = channel.texts.discordFormat.get(String.class)
                    .replace("%user%", senderName)
                    .replace("%userprefix%", Placeholder.stripToDiscord(affixes.prefix()))
                    .replace("%usersuffix%", Placeholder.stripToDiscord(affixes.suffix()))
                    .replace("%message%", message)
                    .replace("%server%", server);
            DiscordBridge.send(channel.discord.channelId, channel.discord.embedTitle, channel.discord.embedColor, discordText);
        }
        return line;
    }

    /** Sends an already formatted line to everyone who may read the channel on this proxy. */
    public static void broadcastLocal(ChatChannel channel, String line) {
        final String permission = channel.settings.usePermission.get(String.class);
        final BaseComponent component = TextComponent.fromLegacy(line);
        for (ProxiedPlayer recipient : ProxyServer.getInstance().getPlayers()) {
            if (recipient.hasPermission(permission)
                    && !channel.isHidden(recipient.getUniqueId())
                    && !channel.isBlocked(recipient)) {
                recipient.sendMessage(component);
            }
        }
    }

    /** Delivers a line received through Redis: server restrictions were already applied by the sender's proxy. */
    public static void broadcastLocalIgnoringServers(ChatChannel channel, String line) {
        final String permission = channel.settings.usePermission.get(String.class);
        final BaseComponent component = TextComponent.fromLegacy(line);
        for (ProxiedPlayer recipient : ProxyServer.getInstance().getPlayers()) {
            if (recipient.hasPermission(permission) && !channel.isHidden(recipient.getUniqueId())) {
                recipient.sendMessage(component);
            }
        }
    }

    private static void applyCooldown(CleanStaffChat plugin, ProxiedPlayer player) {
        if (player.hasPermission(BungeeConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) return;
        final UUID uuid = player.getUniqueId();
        PlayerCache.getCooldown().add(uuid);
        ProxyServer.getInstance().getScheduler().schedule(plugin,
                () -> PlayerCache.getCooldown().remove(uuid),
                BungeeConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);
    }
}
