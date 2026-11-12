package it.frafol.cleanstaffchat.bukkit.channel;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main command of a channel: without arguments it toggles talk mode, with arguments it sends a message.
 */
public class ChannelCommand extends CommandBase {

    private final ChatChannel channel;

    public ChannelCommand(CleanStaffChat plugin, ChatChannel channel, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
        this.channel = channel;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 0) {
            toggleTalkMode(sender);
            return false;
        }

        if (!channel.canUse(sender)) {
            channel.reply(sender, SpigotMessages.NO_PERMISSION);
            return false;
        }

        if (channel.isMuted()) {
            channel.reply(sender, channel.texts.mutedError);
            return false;
        }

        final String message = String.join(" ", args);

        if (sender instanceof Player) {
            ChannelMessenger.deliver(plugin, channel, (Player) sender, message);
            return false;
        }

        if (SpigotConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {
            ChannelMessenger.deliverFromConsole(plugin, channel, sender, message);
        } else {
            channel.reply(sender, SpigotMessages.PLAYER_ONLY);
        }
        return false;
    }

    private void toggleTalkMode(CommandSender sender) {
        if (!inGameCheck(sender, channel.texts.arguments.color().replace("%prefix%", channel.prefix()))) return;
        final Player player = (Player) sender;

        if (!channel.canUse(player)) {
            if (Boolean.FALSE.equals(SpigotConfig.HIDE_ADVERTS.get(Boolean.class))) {
                sender.sendMessage("§7This server is using §dCleanStaffChat §7by §dfrafol§7.");
            }
            return;
        }

        if (channel.isTalking(player.getUniqueId())) {
            channel.setTalking(player.getUniqueId(), false);
            channel.reply(sender, channel.texts.talkDisabled);
            return;
        }

        if (!channel.settings.talkModule.get(Boolean.class) || channel.isMuted()) {
            channel.reply(sender, channel.texts.arguments);
            return;
        }

        channel.startTalking(player.getUniqueId());
        channel.reply(sender, channel.texts.talkEnabled);
    }
}
