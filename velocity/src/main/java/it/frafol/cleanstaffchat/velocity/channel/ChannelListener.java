package it.frafol.cleanstaffchat.velocity.channel;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityDiscordConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Chat listener for one {@link ChatChannel}: handles in-game chat (prefix symbol and talk mode)
 * and messages coming from the channel's Discord text channel.
 */
public class ChannelListener extends ListenerAdapter {

    private final CleanStaffChat plugin;
    private final ChatChannel channel;

    public ChannelListener(CleanStaffChat plugin, ChatChannel channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final String raw = event.getMessage();

        final String symbol = channel.settings.prefixSymbol.get(String.class);
        if (channel.settings.prefixModule.get(Boolean.class) && !symbol.isEmpty() && raw.startsWith(symbol)) {
            if (!channel.canUse(player)) return;
            if (player.getCurrentServer().isEmpty()) return;
            if (rejectIfBlocked(event, player, false)) return;
            denyUnlessWorkaround(event);
            ChannelMessenger.deliver(plugin, channel, player, raw.substring(symbol.length()));
            return;
        }

        if (!channel.isTalking(uuid)) return;
        if (!channel.canUse(player)) {
            channel.setTalking(uuid, false);
            ChatUtil.sendChannelMessage(player, false);
            return;
        }

        if (!channel.settings.talkModule.get(Boolean.class)) {
            channel.reply(player, VelocityMessages.MODULE_DISABLED);
            return;
        }

        if (raw.startsWith("/")) return;
        if (channel.isMuted()) {
            channel.setTalking(uuid, false);
            ChatUtil.sendChannelMessage(player, false);
            event.setResult(PlayerChatEvent.ChatResult.denied());
            channel.reply(player, channel.texts.mutedError);
            return;
        }

        if (channel.hasCooldown() && PlayerCache.getCooldown().contains(uuid)) {
            channel.setTalking(uuid, false);
            ChatUtil.sendChannelMessage(player, false);
            event.setResult(PlayerChatEvent.ChatResult.denied());
            channel.reply(player, VelocityMessages.DONORCHAT_COOLDOWN_MESSAGE);
            return;
        }

        if (player.getCurrentServer().isEmpty()) return;
        if (rejectIfBlocked(event, player, true)) return;

        denyUnlessWorkaround(event);
        ChannelMessenger.deliver(plugin, channel, player, raw);
    }

    /** Handles a player standing on a server where the channel is disabled. */
    private boolean rejectIfBlocked(PlayerChatEvent event, Player player, boolean leaveTalkMode) {
        if (!channel.isBlocked(player)) return false;
        if (leaveTalkMode) channel.setTalking(player.getUniqueId(), false);
        event.setResult(PlayerChatEvent.ChatResult.denied());
        ChatUtil.sendChannelMessage(player, false);
        channel.reply(player, channel.texts.mutedError);
        return true;
    }

    /** With the double-message workaround the proxy must not deny the message: the backend server does. */
    private void denyUnlessWorkaround(PlayerChatEvent event) {
        if (!VelocityConfig.DOUBLE_MESSAGE.get(Boolean.class)) event.setResult(PlayerChatEvent.ChatResult.denied());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (plugin.getConfigTextFile() == null || plugin.getMessagesTextFile() == null) return;
        if (!event.getChannel().getId().equalsIgnoreCase(channel.discord.channelId.get(String.class))) return;

        final Message message = event.getMessage();
        final String content = message.getContentDisplay();

        if (isOwnErrorMessage(content)) {
            deleteLater(message);
            return;
        }

        if (event.getAuthor().isBot() && !VelocityDiscordConfig.FORWARD_BOT.get(Boolean.class)) return;
        if (channel.isMuted()) {
            rejectWith(message, VelocityMessages.STAFFCHAT_MUTED_ERROR_DISCORD);
            return;
        }

        final String authorId = event.getAuthor().getId();
        final boolean discordCooldown = channel.hasCooldown() && !VelocityConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class);
        if (discordCooldown && PlayerCache.getCooldown_discord().contains(authorId)) {
            rejectWith(message, VelocityMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD);
            return;
        }

        final Member member = event.getMember();
        final String username = member != null && member.getNickname() != null
                ? member.getNickname() : event.getAuthor().getName();

        if (ChannelMessenger.isRedisEnabled()) {
            ChannelMessenger.publish(channel.redisMessageChannel, channel.texts.fromDiscordFormat.get(String.class)
                    .replace("%user%", event.getAuthor().getName())
                    .replace("%username%", username)
                    .replace("%message%", content)
                    .replace("%prefix%", channel.prefix())
                    .replace("&", "§"));
        } else {
            ChannelMessenger.broadcastLocal(channel, channel.texts.fromDiscordFormat,
                    new Placeholder("user", event.getAuthor().getName()),
                    new Placeholder("username", username),
                    new Placeholder("message", content),
                    new Placeholder("prefix", channel.prefix()));
        }

        if (discordCooldown) {
            PlayerCache.getCooldown_discord().add(authorId);
            plugin.getServer().getScheduler()
                    .buildTask(plugin, () -> PlayerCache.getCooldown_discord().remove(authorId))
                    .delay(VelocityConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }
    }

    private boolean isOwnErrorMessage(String content) {
        if (content.equalsIgnoreCase(VelocityMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) return true;
        return channel.hasCooldown()
                && content.equalsIgnoreCase(VelocityMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class));
    }

    private void rejectWith(Message message, VelocityMessages error) {
        message.reply(error.get(String.class)).queue();
        deleteLater(message);
    }

    private void deleteLater(Message message) {
        plugin.getServer().getScheduler()
                .buildTask(plugin, () -> message.delete().queue())
                .delay(5, TimeUnit.SECONDS)
                .schedule();
    }
}
