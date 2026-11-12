package it.frafol.cleanstaffchat.velocity.channel;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import org.jetbrains.annotations.NotNull;

/**
 * Mutes or unmutes a channel for everybody. Each channel keeps its own mute state;
 * with RedisBungee the toggle is published so every proxy flips its own state.
 */
public class ChannelMuteCommand implements SimpleCommand {

    private final ChatChannel channel;

    public ChannelMuteCommand(ChatChannel channel) {
        this.channel = channel;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {
        final CommandSource source = invocation.source();

        if (!channel.settings.muteModule.get(Boolean.class)) {
            channel.reply(source, VelocityMessages.MODULE_DISABLED);
            return;
        }

        if (!source.hasPermission(channel.settings.mutePermission.get(String.class))) {
            channel.reply(source, VelocityMessages.NO_PERMISSION);
            return;
        }

        final boolean nowMuted = !channel.isMuted();
        channel.reply(source, nowMuted ? channel.texts.muted : channel.texts.unmuted);
        if (ChannelMessenger.isRedisEnabled()) ChannelMessenger.publish(channel.redisMuteChannel, "set." + channel.name().toLowerCase() + ".mute");
        else channel.setMuted(nowMuted);
    }
}
