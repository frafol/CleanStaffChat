package it.frafol.cleanstaffchat.bungee.channel;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

/**
 * Main command of a channel: without arguments it toggles talk mode, with arguments it sends a message.
 */
public class ChannelCommand extends Command {

    private final CleanStaffChat plugin;
    private final ChatChannel channel;

    public ChannelCommand(CleanStaffChat plugin, ChatChannel channel, List<String> labels) {
        super(labels.get(0), "", labels.toArray(new String[0]));
        this.plugin = plugin;
        this.channel = channel;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            toggleTalkMode(sender);
            return;
        }

        if (!channel.canUse(sender)) {
            channel.reply(sender, BungeeMessages.NO_PERMISSION);
            return;
        }

        if (channel.isMuted()) {
            channel.reply(sender, channel.texts.mutedError);
            return;
        }

        final String message = String.join(" ", args);

        if (sender instanceof ProxiedPlayer player) {
            if (channel.isBlocked(player)) {
                channel.reply(sender, channel.texts.mutedError);
                return;
            }
            ChannelMessenger.deliver(plugin, channel, player, message);
            return;
        }

        if (BungeeConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {
            ChannelMessenger.deliverFromConsole(channel, sender, message);
        } else {
            channel.reply(sender, BungeeMessages.PLAYER_ONLY);
        }
    }

    private void toggleTalkMode(CommandSender sender) {
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(TextComponent.fromLegacy(channel.texts.arguments.color()
                    .replace("%prefix%", channel.prefix())));
            return;
        }

        if (channel.isBlocked(player)) {
            channel.reply(sender, channel.texts.mutedError);
            return;
        }

        if (!channel.canUse(player)) {
            if (Boolean.FALSE.equals(BungeeConfig.HIDE_ADVERTS.get(Boolean.class))) {
                sender.sendMessage(TextComponent.fromLegacy("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));
            }
            return;
        }

        if (channel.isTalking(player.getUniqueId())) {
            channel.setTalking(player.getUniqueId(), false);
            PlayerCache.sendChannelMessage(player, false);
            channel.reply(sender, channel.texts.talkDisabled);
            return;
        }

        if (!channel.settings.talkModule.get(Boolean.class) || channel.isMuted()) {
            channel.reply(sender, channel.texts.arguments);
            return;
        }

        channel.startTalking(player.getUniqueId());
        PlayerCache.sendChannelMessage(player, true);
        channel.reply(sender, channel.texts.talkEnabled);
    }
}
