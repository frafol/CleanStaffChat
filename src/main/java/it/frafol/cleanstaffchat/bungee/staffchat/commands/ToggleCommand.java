package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super(BungeeCommandsConfig.STAFFCHAT_TOGGLE.getStringList().get(0),"", BungeeCommandsConfig.STAFFCHAT_TOGGLE.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(BungeeConfig.STAFFCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.PLAYER_ONLY.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission(BungeeConfig.STAFFCHAT_TOGGLE_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!PlayerCache.getToggled().contains(player.getUniqueId())) {
            PlayerCache.getToggled().add(player.getUniqueId());
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFFCHAT_TOGGLED_OFF.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        PlayerCache.getToggled().remove(player.getUniqueId());
        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFFCHAT_TOGGLED_ON.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())));
    }
}
