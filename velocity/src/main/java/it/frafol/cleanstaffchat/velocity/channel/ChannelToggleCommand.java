package it.frafol.cleanstaffchat.velocity.channel;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import org.jetbrains.annotations.NotNull;

/** Lets a player stop (or resume) receiving the messages of a channel. */
public class ChannelToggleCommand implements SimpleCommand {

    private final ChatChannel channel;

    public ChannelToggleCommand(ChatChannel channel) {
        this.channel = channel;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {
        final CommandSource source = invocation.source();

        if (!channel.settings.toggleModule.get(Boolean.class)) {
            channel.reply(source, VelocityMessages.MODULE_DISABLED);
            return;
        }

        if (!(source instanceof Player player)) {
            channel.reply(source, VelocityMessages.PLAYER_ONLY);
            return;
        }

        if (!player.hasPermission(channel.settings.togglePermission.get(String.class))) {
            channel.reply(source, VelocityMessages.NO_PERMISSION);
            return;
        }

        final boolean nowHidden = !channel.isHidden(player.getUniqueId());
        channel.setHidden(player.getUniqueId(), nowHidden);
        channel.reply(source, nowHidden ? channel.texts.toggledOff : channel.texts.toggledOn);
    }
}
