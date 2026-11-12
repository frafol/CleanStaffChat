package it.frafol.cleanstaffchat.bungee.channel;

import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

/** Lets a player stop (or resume) receiving the messages of a channel. */
public class ChannelToggleCommand extends Command {

    private final ChatChannel channel;

    public ChannelToggleCommand(ChatChannel channel, List<String> labels) {
        super(labels.get(0), "", labels.toArray(new String[0]));
        this.channel = channel;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!channel.settings.toggleModule.get(Boolean.class)) {
            channel.reply(sender, BungeeMessages.MODULE_DISABLED);
            return;
        }

        if (!(sender instanceof ProxiedPlayer player)) {
            channel.reply(sender, BungeeMessages.PLAYER_ONLY);
            return;
        }

        if (!player.hasPermission(channel.settings.togglePermission.get(String.class))) {
            channel.reply(sender, BungeeMessages.NO_PERMISSION);
            return;
        }

        final boolean nowHidden = !channel.isHidden(player.getUniqueId());
        channel.setHidden(player.getUniqueId(), nowHidden);
        channel.reply(sender, nowHidden ? channel.texts.toggledOff : channel.texts.toggledOn);
    }
}
