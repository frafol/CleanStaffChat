package it.frafol.cleanstaffchat.bukkit.channel;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Chat listener for one {@link ChatChannel}: handles in-game chat (prefix symbol and talk mode)
 * and messages coming from the channel's Discord text channel.
 */
public class ChannelListener extends ListenerAdapter implements Listener {

    private static final long DELETE_DELAY_TICKS = 5L * 20L;

    private final CleanStaffChat plugin;
    private final ChatChannel channel;

    public ChannelListener(CleanStaffChat plugin, ChatChannel channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final String raw = event.getMessage();

        if (channel.respectsPunishmentMute() && plugin.isMuted(player)) return;
        final String symbol = channel.settings.prefixSymbol.get(String.class);
        if (channel.settings.prefixModule.get(Boolean.class) && !symbol.isEmpty() && raw.startsWith(symbol)) {
            if (!channel.canUse(player)) return;
            event.setCancelled(true);
            ChannelMessenger.deliver(plugin, channel, player, raw.substring(symbol.length()));
            return;
        }

        if (!channel.isTalking(uuid)) return;
        if (channel.isMuted()) {
            channel.setTalking(uuid, false);
            event.setCancelled(true);
            channel.reply(player, channel.texts.mutedError);
            return;
        }

        if (channel.hasCooldown() && PlayerCache.getCooldown().contains(uuid)) {
            channel.setTalking(uuid, false);
            event.setCancelled(true);
            channel.reply(player, SpigotMessages.DONORCHAT_COOLDOWN_MESSAGE);
            return;
        }

        if (raw.startsWith("/")) return;
        if (!channel.settings.talkModule.get(Boolean.class)) {
            channel.reply(player, SpigotMessages.MODULE_DISABLED);
            return;
        }

        if (!channel.canUse(player)) {
            channel.setTalking(uuid, false);
            return;
        }

        event.setCancelled(true);
        ChannelMessenger.deliver(plugin, channel, player, raw);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (CleanStaffChat.getInstance() == null) return;
        if (!event.getChannel().getId().equalsIgnoreCase(channel.discord.channelId.get(String.class))) return;

        final Message message = event.getMessage();
        final String content = message.getContentDisplay();
        if (isOwnErrorMessage(content)) {
            deleteLater(message);
            return;
        }

        if (event.getAuthor().isBot() && !SpigotDiscordConfig.FORWARD_BOT.get(Boolean.class)) return;

        if (channel.isMuted()) {
            rejectWith(message, SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD);
            return;
        }

        final String authorId = event.getAuthor().getId();
        final boolean discordCooldown = channel.hasCooldown() && !SpigotConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class);
        if (discordCooldown && PlayerCache.getCooldown_discord().contains(authorId)) {
            rejectWith(message, SpigotMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD);
            return;
        }

        final Member member = event.getMember();
        final String username = member != null && member.getNickname() != null
                ? member.getNickname() : event.getAuthor().getName();

        final String line = channel.texts.fromDiscordFormat.color()
                .replace("%prefix%", channel.prefix())
                .replace("%user%", event.getAuthor().getName())
                .replace("%username%", username)
                .replace("%message%", content)
                .replace("&", "§");

        final String permission = channel.settings.usePermission.get(String.class);
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.hasPermission(permission) && !channel.isHidden(recipient.getUniqueId())) {
                recipient.sendMessage(line);
            }
        }

        if (discordCooldown) {
            PlayerCache.getCooldown_discord().add(authorId);
            UniversalScheduler.getScheduler(plugin).runTaskLaterAsynchronously(
                    () -> PlayerCache.getCooldown_discord().remove(authorId),
                    SpigotConfig.DONOR_TIMER.get(Integer.class) * 20L);
        }
    }

    private boolean isOwnErrorMessage(String content) {
        if (content.equalsIgnoreCase(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) return true;
        return channel.hasCooldown()
                && content.equalsIgnoreCase(SpigotMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class));
    }

    private void rejectWith(Message message, SpigotMessages error) {
        message.reply(error.get(String.class)).queue();
        deleteLater(message);
    }

    private void deleteLater(Message message) {
        UniversalScheduler.getScheduler(plugin).runTaskLaterAsynchronously(
                () -> message.delete().queue(), DELETE_DELAY_TICKS);
    }
}
