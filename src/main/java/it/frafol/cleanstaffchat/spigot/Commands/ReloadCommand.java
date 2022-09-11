package it.frafol.cleanstaffchat.spigot.Commands;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.TextFile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

public class ReloadCommand implements CommandExecutor {

    public final CleanStaffChat plugin;

    public ReloadCommand(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("screload")
                || command.getName().equalsIgnoreCase("staffchatreload")
                || command.getName().equalsIgnoreCase("cleanscreload")
                || command.getName().equalsIgnoreCase("cleanstaffchatreload")) {
            if (sender.hasPermission(SpigotConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
                TextFile.reloadAll();
                sender.sendMessage((SpigotConfig.RELOADED.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color())));
            } else {
                sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color())));
            }
        }
        return false;
    }
}
