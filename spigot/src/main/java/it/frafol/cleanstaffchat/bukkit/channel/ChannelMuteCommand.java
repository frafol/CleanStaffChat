package it.frafol.cleanstaffchat.bukkit.channel;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Mutes or unmutes a channel for everybody. Each channel keeps its own mute state.
 */
public class ChannelMuteCommand extends CommandBase {

    private final ChatChannel channel;

    public ChannelMuteCommand(CleanStaffChat plugin, ChatChannel channel, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
        this.channel = channel;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!channel.settings.muteModule.get(Boolean.class)) {
            channel.reply(sender, SpigotMessages.MODULE_DISABLED);
            return false;
        }

        if (!sender.hasPermission(channel.settings.mutePermission.get(String.class))) {
            channel.reply(sender, SpigotMessages.NO_PERMISSION);
            return false;
        }

        final boolean nowMuted = !channel.isMuted();
        channel.setMuted(nowMuted);
        channel.reply(sender, nowMuted ? channel.texts.muted : channel.texts.unmuted);
        return false;
    }
}
