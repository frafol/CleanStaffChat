package it.frafol.cleanstaffchat.spigot.Commands;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand implements CommandExecutor {

    public final CleanStaffChat plugin;

    public ToggleCommand(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(SpigotConfig.STAFFCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage((SpigotConfig.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotConfig.PREFIX.color())));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage((SpigotConfig.PLAYER_ONLY.color()
                    .replace("%prefix%", SpigotConfig.PREFIX.color())));
            return false;
        }

        Player player = (Player) sender;

        if (player.hasPermission(SpigotConfig.STAFFCHAT_TOGGLE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getToggled().contains(player.getUniqueId())) {
                PlayerCache.getToggled().add(player.getUniqueId());
                sender.sendMessage((SpigotConfig.STAFFCHAT_TOGGLED_OFF.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color())));
                return false;
            }
        } else {
            sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotConfig.PREFIX.color())));
            return false;
        }

        PlayerCache.getToggled().remove(player.getUniqueId());

        sender.sendMessage((SpigotConfig.STAFFCHAT_TOGGLED_ON.color()
                .replace("%prefix%", SpigotConfig.PREFIX.color())));
        return false;
    }
}