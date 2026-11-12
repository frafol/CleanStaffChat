package it.frafol.cleanstaffchat.bukkit.channel;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.AffixResolver;
import it.frafol.cleanstaffchat.bukkit.objects.Affixes;
import it.frafol.cleanstaffchat.bukkit.objects.ChatFormat;
import it.frafol.cleanstaffchat.bukkit.objects.DiscordBridge;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Delivers a message to a channel: in-game recipients and Discord.
 * Used by both the commands and the chat listeners, so the rules exist only once.
 */
public final class ChannelMessenger {

    private ChannelMessenger() {
    }

    /**
     * Validates and delivers a message written by a player.
     *
     * @return false when the message was rejected (muted, color codes, cooldown, permission data missing)
     */
    public static boolean deliver(CleanStaffChat plugin, ChatChannel channel, Player player, String message) {
        if (channel.isMuted()) {
            channel.reply(player, channel.texts.mutedError);
            return false;
        }

        if (SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class) && PlayerCache.hasColorCodes(message)) {
            player.sendMessage(SpigotMessages.COLOR_CODES.color()
                    .replace("%prefix%", channel.prefix())
                    .replace("&", "§"));
            return false;
        }

        if (channel.hasCooldown() && PlayerCache.getCooldown().contains(player.getUniqueId())) {
            channel.reply(player, SpigotMessages.DONORCHAT_COOLDOWN_MESSAGE);
            return false;
        }

        final Affixes affixes = AffixResolver.resolve(player);
        if (affixes == null) return false;

        dispatch(plugin, channel, player.getName(), channel.texts.format.color(player), affixes, message);

        if (channel.hasCooldown()) applyCooldown(plugin, player);
        return true;
    }

    /** Delivers a message written from the console and echoes it back to the console. */
    public static void deliverFromConsole(CleanStaffChat plugin, ChatChannel channel, CommandSender console, String message) {
        final String line = dispatch(plugin, channel, SpigotConfig.CONSOLE_PREFIX.get(String.class),
                channel.texts.consoleFormat.color(), Affixes.NONE, message);
        console.sendMessage(line);
    }

    private static String dispatch(CleanStaffChat plugin, ChatChannel channel, String senderName,
                                   String template, Affixes affixes, String message) {

        final String line = ChatFormat.apply(template, channel.prefix(), senderName, affixes)
                .replace("%message%", message)
                .replace("&", "§");

        final String permission = channel.settings.usePermission.get(String.class);
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.hasPermission(permission) && !channel.isHidden(recipient.getUniqueId())) {
                recipient.sendMessage(line);
            }
        }

        if (DiscordBridge.isActive(channel.settings.discordModule)) {
            final String discordText = ChatFormat
                    .applyDiscord(channel.texts.discordFormat.get(String.class), senderName, affixes)
                    .replace("%message%", message);
            DiscordBridge.send(plugin, channel.discord.channelId, channel.discord.embedTitle,
                    channel.discord.embedColor, discordText);
        }

        return line;
    }

    private static void applyCooldown(CleanStaffChat plugin, Player player) {
        if (player.hasPermission(SpigotConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) return;
        final UUID uuid = player.getUniqueId();
        PlayerCache.getCooldown().add(uuid);
        UniversalScheduler.getScheduler(plugin).runTaskLaterAsynchronously(
                () -> PlayerCache.getCooldown().remove(uuid),
                SpigotConfig.DONOR_TIMER.get(Integer.class) * 20L);
    }
}
