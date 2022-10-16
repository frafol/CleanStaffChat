package it.frafol.cleanstaffchat.bungee.Commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MuteCommand extends Command {

    public MuteCommand() {
        super("scmute","","staffchatmute","cleanscmute","cleanstaffchatmute", "staffmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(BungeeConfig.STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage(new TextComponent(BungeeConfig.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())));
            return;
        }

        if (sender.hasPermission(BungeeConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                PlayerCache.getMuted().add("true");
                sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_MUTED.color()
                        .replace("%prefix%", BungeeConfig.PREFIX.color())));
            } else {
                PlayerCache.getMuted().remove("true");
                sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_UNMUTED.color()
                        .replace("%prefix%", BungeeConfig.PREFIX.color())));
            }
        } else {
            sender.sendMessage(new TextComponent(BungeeConfig.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())));
        }
    }
}
