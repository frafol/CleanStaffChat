package it.frafol.cleanstaffchat.bukkit.channel;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lets a player stop (or resume) receiving the messages of a channel.
 */
public class ChannelToggleCommand extends CommandBase {

    private final ChatChannel channel;

    public ChannelToggleCommand(CleanStaffChat plugin, ChatChannel channel, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
        this.channel = channel;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!channel.settings.toggleModule.get(Boolean.class)) {
            channel.reply(sender, SpigotMessages.MODULE_DISABLED);
            return false;
        }

        if (!inGameCheck(sender, channel.texts.arguments.color().replace("%prefix%", channel.prefix()))) return false;
        final Player player = (Player) sender;

        if (!player.hasPermission(channel.settings.togglePermission.get(String.class))) {
            channel.reply(sender, SpigotMessages.NO_PERMISSION);
            return false;
        }

        final boolean nowHidden = !channel.isHidden(player.getUniqueId());
        channel.setHidden(player.getUniqueId(), nowHidden);
        channel.reply(sender, nowHidden ? channel.texts.toggledOff : channel.texts.toggledOn);
        return false;
    }
}
