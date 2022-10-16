package it.frafol.cleanstaffchat.bungee.Commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("sctoggle","","staffchattoggle","cleansctoggle","cleanstaffchattoggle","stafftoggle");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(BungeeConfig.STAFFCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage(new TextComponent(BungeeConfig.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())));
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(BungeeConfig.PLAYER_ONLY.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (player.hasPermission(BungeeConfig.STAFFCHAT_TOGGLE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getToggled().contains(player.getUniqueId())) {
                PlayerCache.getToggled().add(player.getUniqueId());
                sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_TOGGLED_OFF.color()
                        .replace("%prefix%", BungeeConfig.PREFIX.color())));
                return;
            }
        } else {
            sender.sendMessage(new TextComponent(BungeeConfig.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())));
            return;
        }

        PlayerCache.getToggled().remove(player.getUniqueId());

        sender.sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_TOGGLED_ON.color()
                .replace("%prefix%", BungeeConfig.PREFIX.color())));
    }
}
