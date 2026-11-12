package it.frafol.cleanstaffchat.bungee.channel;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Chat listener for one {@link ChatChannel}: handles in-game chat (prefix symbol and talk mode)
 * and messages coming from the channel's Discord text channel.
 */
public class ChannelListener extends ListenerAdapter implements Listener {

    private final CleanStaffChat plugin;
    private final ChatChannel channel;

    public ChannelListener(CleanStaffChat plugin, ChatChannel channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    @EventHandler
    public void onChat(@NotNull ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player)) return;
        final UUID uuid = player.getUniqueId();
        final String raw = event.getMessage();
        final String symbol = channel.settings.prefixSymbol.get(String.class);
        if (channel.settings.prefixModule.get(Boolean.class) && !symbol.isEmpty() && raw.startsWith(symbol)) {
            if (!channel.canUse(player)) return;
            if (rejectIfBlocked(event, player, false)) return;
            cancelUnlessWorkaround(event);
            ChannelMessenger.deliver(plugin, channel, player, raw.substring(symbol.length()));
            return;
        }

        if (!channel.isTalking(uuid)) return;
        if (channel.isMuted()) {
            channel.setTalking(uuid, false);
            PlayerCache.sendChannelMessage(player, false);
            event.setCancelled(true);
            channel.reply(player, channel.texts.mutedError);
            return;
        }

        if (channel.hasCooldown() && PlayerCache.getCooldown().contains(uuid)) {
            channel.setTalking(uuid, false);
            PlayerCache.sendChannelMessage(player, false);
            event.setCancelled(true);
            channel.reply(player, BungeeMessages.DONORCHAT_COOLDOWN_MESSAGE);
            return;
        }

        if (raw.startsWith("/")) return;
        if (rejectIfBlocked(event, player, true)) return;
        if (!channel.settings.talkModule.get(Boolean.class)) {
            channel.reply(player, BungeeMessages.MODULE_DISABLED);
            return;
        }

        if (!channel.canUse(player)) {
            channel.setTalking(uuid, false);
            PlayerCache.sendChannelMessage(player, false);
            return;
        }

        cancelUnlessWorkaround(event);
        ChannelMessenger.deliver(plugin, channel, player, raw);
    }

    /** Handles a player standing on a server where the channel is disabled. */
    private boolean rejectIfBlocked(ChatEvent event, ProxiedPlayer player, boolean leaveTalkMode) {
        if (!channel.isBlocked(player)) return false;
        if (leaveTalkMode) channel.setTalking(player.getUniqueId(), false);
        event.setCancelled(true);
        PlayerCache.sendChannelMessage(player, false);
        channel.reply(player, channel.texts.mutedError);
        return true;
    }

    /** With the kick workaround the proxy must not cancel the message: the backend server does. */
    private void cancelUnlessWorkaround(ChatEvent event) {
        if (!BungeeConfig.WORKAROUND_KICK.get(Boolean.class)) event.setCancelled(true);
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

        if (event.getAuthor().isBot() && !BungeeDiscordConfig.FORWARD_BOT.get(Boolean.class)) return;
        if (channel.isMuted()) {
            rejectWith(message, BungeeMessages.STAFFCHAT_MUTED_ERROR_DISCORD);
            return;
        }

        final String authorId = event.getAuthor().getId();
        final boolean discordCooldown = channel.hasCooldown() && !BungeeConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class);
        if (discordCooldown && PlayerCache.getCooldown_discord().contains(authorId)) {
            rejectWith(message, BungeeMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD);
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

        if (ChannelMessenger.isRedisEnabled()) ChannelMessenger.publish(channel.redisMessageChannel, line);
        else ChannelMessenger.broadcastLocal(channel, line);
        if (discordCooldown) {
            PlayerCache.getCooldown_discord().add(authorId);
            ProxyServer.getInstance().getScheduler().schedule(plugin,
                    () -> PlayerCache.getCooldown_discord().remove(authorId),
                    BungeeConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);
        }
    }

    private boolean isOwnErrorMessage(String content) {
        if (content.equalsIgnoreCase(BungeeMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) return true;
        return channel.hasCooldown()
                && content.equalsIgnoreCase(BungeeMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class));
    }

    private void rejectWith(Message message, BungeeMessages error) {
        message.reply(error.get(String.class)).queue();
        deleteLater(message);
    }

    private void deleteLater(Message message) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> message.delete().queue(), 5, TimeUnit.SECONDS);
    }
}
