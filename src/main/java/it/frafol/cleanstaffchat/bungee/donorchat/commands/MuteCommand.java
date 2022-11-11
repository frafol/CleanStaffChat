package it.frafol.cleanstaffchat.bungee.donorchat.commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MuteCommand extends Command {

    public MuteCommand() {
        super(BungeeCommandsConfig.DONORCHAT_MUTE.getStringList().get(0),"", BungeeCommandsConfig.DONORCHAT_MUTE.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(BungeeConfig.DONORCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
            return;
        }

        if (sender.hasPermission(BungeeConfig.DONORCHAT_MUTE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted_donor().contains("true")) {
                PlayerCache.getMuted_donor().add("true");
                sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.DONORCHAT_MUTED.color()
                        .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
            } else {
                PlayerCache.getMuted_donor().remove("true");
                sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.DONORCHAT_UNMUTED.color()
                        .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
        }
    }
}
