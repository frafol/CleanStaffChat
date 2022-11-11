package it.frafol.cleanstaffchat.bukkit.staffchat.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MuteCommand implements CommandExecutor {

    public final CleanStaffChat plugin;

    public MuteCommand(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] strings) {

        if (!(SpigotConfig.STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {

            sender.sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));

        }

        if (sender.hasPermission(SpigotConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {

            if (!PlayerCache.getMuted().contains("true")) {

                PlayerCache.getMuted().add("true");

                sender.sendMessage((SpigotMessages.STAFFCHAT_MUTED.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())));

            } else {

                PlayerCache.getMuted().remove("true");

                sender.sendMessage((SpigotMessages.STAFFCHAT_UNMUTED.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())));
            }

        } else {

            sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));


        }

        return false;

    }
}
