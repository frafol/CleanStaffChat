package it.frafol.cleanstaffchat.spigot.adminchat.commands;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleCommand implements CommandExecutor {

    public final CleanStaffChat plugin;

    public ToggleCommand(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String s, String[] strings) {
        if (!(SpigotConfig.ADMINCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage((SpigotConfig.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage((SpigotConfig.PLAYER_ONLY.color()
                    .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())));
            return false;
        }

        Player player = (Player) sender;

        if (player.hasPermission(SpigotConfig.ADMINCHAT_TOGGLE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getToggled_admin().contains(player.getUniqueId())) {
                PlayerCache.getToggled_admin().add(player.getUniqueId());
                sender.sendMessage((SpigotConfig.ADMINCHAT_TOGGLED_OFF.color()
                        .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())));
                return false;
            }
        } else {
            sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())));
            return false;
        }

        PlayerCache.getToggled_admin().remove(player.getUniqueId());

        sender.sendMessage((SpigotConfig.ADMINCHAT_TOGGLED_ON.color()
                .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())));
        return false;
    }
}