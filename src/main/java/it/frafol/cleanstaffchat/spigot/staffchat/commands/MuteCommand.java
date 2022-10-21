package it.frafol.cleanstaffchat.spigot.staffchat.commands;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
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
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String s, String[] strings) {

        if (command.getName().equalsIgnoreCase("scmute")
                || command.getName().equalsIgnoreCase("staffchatmute")
                || command.getName().equalsIgnoreCase("cleanscmute")
                || command.getName().equalsIgnoreCase("cleanstaffchatmute")
                || command.getName().equalsIgnoreCase("staffmute")) {

            if (!(SpigotConfig.STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {

                sender.sendMessage((SpigotConfig.MODULE_DISABLED.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color())));

            }

            if (sender.hasPermission(SpigotConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {

                if (!PlayerCache.getMuted().contains("true")) {

                    PlayerCache.getMuted().add("true");

                    sender.sendMessage((SpigotConfig.STAFFCHAT_MUTED.color()
                            .replace("%prefix%", SpigotConfig.PREFIX.color())));

                } else {

                    PlayerCache.getMuted().remove("true");

                    sender.sendMessage((SpigotConfig.STAFFCHAT_UNMUTED.color()
                            .replace("%prefix%", SpigotConfig.PREFIX.color())));
                }

            } else {

                sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color())));


            }
        }

        return false;

    }
}
