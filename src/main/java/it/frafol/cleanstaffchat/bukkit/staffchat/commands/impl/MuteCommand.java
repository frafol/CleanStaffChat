package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MuteCommand extends CommandBase {
    public MuteCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if (!(SpigotConfig.STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
        }

        if (!sender.hasPermission(SpigotConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {
            sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        if (!PlayerCache.getMuted().contains("true")) {
            PlayerCache.getMuted().add("true");
            sender.sendMessage((SpigotMessages.STAFFCHAT_MUTED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));

        } else {
            PlayerCache.getMuted().remove("true");
            sender.sendMessage((SpigotMessages.STAFFCHAT_UNMUTED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
        }
        return false;
    }
}