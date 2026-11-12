package it.frafol.cleanstaffchat.bungee.channel;

import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

/**
 * Mutes or unmutes a channel for everybody. Each channel keeps its own mute state;
 * with RedisBungee the toggle is published so every proxy flips its own state.
 */
public class ChannelMuteCommand extends Command {

    private final ChatChannel channel;

    public ChannelMuteCommand(ChatChannel channel, List<String> labels) {
        super(labels.get(0), "", labels.toArray(new String[0]));
        this.channel = channel;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!channel.settings.muteModule.get(Boolean.class)) {
            channel.reply(sender, BungeeMessages.MODULE_DISABLED);
            return;
        }

        if (!sender.hasPermission(channel.settings.mutePermission.get(String.class))) {
            channel.reply(sender, BungeeMessages.NO_PERMISSION);
            return;
        }

        final boolean nowMuted = !channel.isMuted();
        channel.reply(sender, nowMuted ? channel.texts.muted : channel.texts.unmuted);
        if (ChannelMessenger.isRedisEnabled()) ChannelMessenger.publish(channel.redisMuteChannel, "set." + channel.name().toLowerCase() + ".mute");
        else channel.setMuted(nowMuted);
    }
}
