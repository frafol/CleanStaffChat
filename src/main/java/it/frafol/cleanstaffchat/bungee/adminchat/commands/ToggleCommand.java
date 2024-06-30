package it.frafol.cleanstaffchat.bungee.adminchat.commands;

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
        super(BungeeCommandsConfig.ADMINCHAT_TOGGLE.getStringList().get(0),"", BungeeCommandsConfig.ADMINCHAT_TOGGLE.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(BungeeConfig.ADMINCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.PLAYER_ONLY.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission(BungeeConfig.ADMINCHAT_TOGGLE_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        if (!PlayerCache.getToggled_admin().contains(player.getUniqueId())) {
            PlayerCache.getToggled_admin().add(player.getUniqueId());
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_TOGGLED_OFF.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        PlayerCache.getToggled_admin().remove(player.getUniqueId());
        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_TOGGLED_ON.color()
                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
    }
}
