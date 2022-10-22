package it.frafol.cleanstaffchat.bukkit.donorchat.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
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

        if (!(SpigotConfig.DONORCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage((SpigotConfig.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage((SpigotConfig.PLAYER_ONLY.color()
                    .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));
            return false;
        }

        Player player = (Player) sender;

        if (player.hasPermission(SpigotConfig.DONORCHAT_TOGGLE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getToggled_donor().contains(player.getUniqueId())) {
                PlayerCache.getToggled_donor().add(player.getUniqueId());
                sender.sendMessage((SpigotConfig.DONORCHAT_TOGGLED_OFF.color()
                        .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));
                return false;
            }
        } else {
            sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));
            return false;
        }

        PlayerCache.getToggled_donor().remove(player.getUniqueId());

        sender.sendMessage((SpigotConfig.DONORCHAT_TOGGLED_ON.color()
                .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));
        return false;
    }
}