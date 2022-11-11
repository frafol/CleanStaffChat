package it.frafol.cleanstaffchat.bukkit.staffchat.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.TextFile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    public final CleanStaffChat plugin;

    public ReloadCommand(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.@NotNull CommandSender sender, Command command, @NotNull String s, String[] strings) {

        if (command.getName().equalsIgnoreCase("screload")
                || command.getName().equalsIgnoreCase("staffchatreload")
                || command.getName().equalsIgnoreCase("cleanscreload")
                || command.getName().equalsIgnoreCase("cleanstaffchatreload")
                || command.getName().equalsIgnoreCase("staffreload")) {

            if (sender.hasPermission(SpigotConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {

                TextFile.reloadAll();

                sender.sendMessage((SpigotMessages.RELOADED.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())));

            } else {

                sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())));

            }
        }

        return false;

    }
}
