package it.frafol.cleanstaffchat.bukkit.donorchat.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
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
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String s, String[] strings) {

        if (command.getName().equalsIgnoreCase("dcmute")
                || command.getName().equalsIgnoreCase("donorchatmute")
                || command.getName().equalsIgnoreCase("donormute")) {

            if (!(SpigotConfig.DONORCHAT_MUTE_MODULE.get(Boolean.class))) {

                sender.sendMessage((SpigotConfig.MODULE_DISABLED.color()
                        .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));

            }

            if (sender.hasPermission(SpigotConfig.DONORCHAT_MUTE_PERMISSION.get(String.class))) {

                if (!PlayerCache.getMuted().contains("true")) {

                    PlayerCache.getMuted().add("true");

                    sender.sendMessage((SpigotConfig.DONORCHAT_MUTED.color()
                            .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));

                } else {

                    PlayerCache.getMuted().remove("true");

                    sender.sendMessage((SpigotConfig.DONORCHAT_UNMUTED.color()
                            .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));
                }

            } else {

                sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                        .replace("%prefix%", SpigotConfig.DONORPREFIX.color())));


            }
        }

        return false;

    }
}
