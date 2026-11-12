package it.frafol.cleanstaffchat.velocity.channel;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Main command of a channel: without arguments it toggles talk mode, with arguments it sends a message.
 */
public class ChannelCommand implements SimpleCommand {

    private final CleanStaffChat plugin;
    private final ChatChannel channel;

    public ChannelCommand(CleanStaffChat plugin, ChatChannel channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {
        final CommandSource source = invocation.source();
        final String[] args = invocation.arguments();

        if (args.length == 0) {
            toggleTalkMode(source);
            return;
        }

        if (!channel.canUse(source)) {
            channel.reply(source, VelocityMessages.NO_PERMISSION);
            return;
        }

        if (channel.isMuted()) {
            channel.reply(source, channel.texts.mutedError);
            return;
        }

        final String message = String.join(" ", args);

        if (source instanceof Player player) {
            if (player.getCurrentServer().isEmpty()) return;
            if (channel.isBlocked(player)) {
                channel.reply(source, channel.texts.mutedError);
                return;
            }
            ChannelMessenger.deliver(plugin, channel, player, message);
            return;
        }

        if (VelocityConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {
            ChannelMessenger.deliverFromConsole(channel, source, message);
        } else {
            channel.reply(source, VelocityMessages.PLAYER_ONLY);
        }
    }

    private void toggleTalkMode(CommandSource source) {
        if (!(source instanceof Player player)) {
            channel.reply(source, channel.texts.arguments);
            return;
        }

        if (!channel.canUse(player)) {
            if (Boolean.FALSE.equals(VelocityConfig.HIDE_ADVERTS.get(Boolean.class))) {
                source.sendMessage(Component.text("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));
            }
            return;
        }

        if (channel.isBlocked(player)) {
            channel.reply(source, channel.texts.mutedError);
            return;
        }

        if (channel.isTalking(player.getUniqueId())) {
            channel.setTalking(player.getUniqueId(), false);
            ChatUtil.sendChannelMessage(player, false);
            channel.reply(source, channel.texts.talkDisabled);
            return;
        }

        if (!channel.settings.talkModule.get(Boolean.class) || channel.isMuted()) {
            channel.reply(source, channel.texts.arguments);
            return;
        }

        channel.startTalking(player.getUniqueId());
        ChatUtil.sendChannelMessage(player, true);
        channel.reply(source, channel.texts.talkEnabled);
    }
}
