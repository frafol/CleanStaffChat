package it.frafol.cleanstaffchat.bungee.Commands;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;

public class StaffChatCommand extends Command {

    public StaffChatCommand() {

        super("sc","","staffchat","cleansc","cleanstaffchat");

    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length == 0) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(new TextComponent(BungeeConfig.ARGUMENTS.color()
                        .replace("%prefix%", BungeeConfig.PREFIX.color())));
                return;
            }

            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (sender.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                if (!PlayerCache.getToggled_2().contains(player.getUniqueId())) {
                    if (!(BungeeConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                        sender.sendMessage(new TextComponent(BungeeConfig.MODULE_DISABLED.color()
                                .replace("%prefix%", BungeeConfig.PREFIX.color())));
                        return;
                    }
                    if (!PlayerCache.getMuted().contains("true")) {
                        PlayerCache.getToggled_2().add(player.getUniqueId());
                        sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_TALK_ENABLED.color()
                                .replace("%prefix%", BungeeConfig.PREFIX.color())));
                        return;
                    } else {
                        sender.sendMessage(new TextComponent(BungeeConfig.ARGUMENTS.color()
                                .replace("%prefix%", BungeeConfig.PREFIX.color())));
                    }
                } else if (PlayerCache.getToggled_2().contains(player.getUniqueId())) {
                    PlayerCache.getToggled_2().remove(player.getUniqueId());
                    sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_TALK_DISABLED.color()
                            .replace("%prefix%", BungeeConfig.PREFIX.color())));
                    return;
                }
            } else {
                sender.sendMessage(new TextComponent("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));
            }
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        String commandsender = !(sender instanceof ProxiedPlayer) ? BungeeConfig.CONSOLE_PREFIX.get(String.class) :
                sender.getName();

        if (sender.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                if (sender instanceof ProxiedPlayer) {
                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_FORMAT.color()
                                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                                    .replace("%user%", commandsender)
                                    .replace("%message%", message)
                                    .replace("&", "§"))));
                } else if (BungeeConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {
                    if (!PlayerCache.getMuted().contains("true")) {
                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%message%", message))));
                    } else {
                        sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_MUTED_ERROR.color()
                                .replace("%prefix%", BungeeConfig.PREFIX.color())));
                    }

                    sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_FORMAT.color()
                            .replace("%prefix%", BungeeConfig.PREFIX.color())
                            .replace("%user%", commandsender)
                            .replace("%message%", message)));

                } else {
                    sender.sendMessage(new TextComponent(BungeeConfig.PLAYER_ONLY.color()
                            .replace("%prefix%", BungeeConfig.PREFIX.color())));
                }
            } else {

                sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", BungeeConfig.PREFIX.color())));

            }

        } else {

            sender.sendMessage(new TextComponent(BungeeConfig.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())));

        }
    }
}