package it.frafol.cleanstaffchat.bungee.general.commands;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MuteChatCommand extends Command {

    public MuteChatCommand() {
        super(BungeeCommandsConfig.MUTECHAT.getStringList().get(0),"", BungeeCommandsConfig.MUTECHAT.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender commandSource, String[] args) {

        if (args.length == 0) {

            if (!commandSource.hasPermission(BungeeConfig.MUTECHAT_ALL_PERMISSION.get(String.class))) {
                commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MUTECHAT_DISABLED.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            PlayerCache.getMutedservers().add("all");
            commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MUTECHAT_ENABLED.color()
                    .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
            return;
        }

        if (args.length == 1) {

            if (!(commandSource instanceof ProxiedPlayer)) {
                commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.PLAYER_ONLY.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            if (!commandSource.hasPermission(BungeeConfig.MUTECHAT_PERMISSION.get(String.class))) {
                commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            String server = args[0];

            if (server.equalsIgnoreCase("all")) {

                if (!commandSource.hasPermission(BungeeConfig.MUTECHAT_ALL_PERMISSION.get(String.class))) {
                    commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                            .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                    return;
                }

                if (PlayerCache.getMutedservers().contains("all")) {
                    PlayerCache.getMutedservers().remove("all");
                    commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MUTECHAT_DISABLED.color()
                            .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                    return;
                }

                PlayerCache.getMutedservers().add("all");
                commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MUTECHAT_ENABLED.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            if (!CleanStaffChat.getInstance().getProxy().getServers().containsKey(server)) {
                commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.SERVER_NOT_FOUND.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            if (PlayerCache.getMutedservers().contains(server)) {
                PlayerCache.getMutedservers().remove(server);
                commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MUTECHAT_DISABLED.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            PlayerCache.getMutedservers().add(server);
            commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MUTECHAT_ENABLED.color()
                    .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));

        } else {
            commandSource.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MUTECHAT_USAGE.color()
                    .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
        }
    }
}
