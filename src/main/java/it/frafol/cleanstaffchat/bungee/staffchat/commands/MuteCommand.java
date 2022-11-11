package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MuteCommand extends Command {

    public MuteCommand() {
        super(BungeeCommandsConfig.STAFFCHAT_MUTE.getStringList().get(0),"",BungeeCommandsConfig.STAFFCHAT_MUTE.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(BungeeConfig.STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (sender.hasPermission(BungeeConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                PlayerCache.getMuted().add("true");
                sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_MUTED.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())));
            } else {
                PlayerCache.getMuted().remove("true");
                sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_UNMUTED.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
        }
    }
}
