package it.frafol.cleanstaffchat.bungee.adminchat.commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MuteCommand extends Command {

    public MuteCommand() {
        super("acmute","","adminchatmute","adminmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(BungeeConfig.ADMINCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeConfig.ADMINPREFIX.color())));
            return;
        }

        if (sender.hasPermission(BungeeConfig.ADMINCHAT_MUTE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted_admin().contains("true")) {
                PlayerCache.getMuted_admin().add("true");
                sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.ADMINCHAT_MUTED.color()
                        .replace("%prefix%", BungeeConfig.ADMINPREFIX.color())));
            } else {
                PlayerCache.getMuted_admin().remove("true");
                sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.ADMINCHAT_UNMUTED.color()
                        .replace("%prefix%", BungeeConfig.ADMINPREFIX.color())));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeConfig.ADMINPREFIX.color())));
        }
    }
}
