package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToggleCommand extends CommandBase {

    public ToggleCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {

        if (!(SpigotConfig.STAFFCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        if (!inGameCheck(sender)) {
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(SpigotConfig.STAFFCHAT_TOGGLE_PERMISSION.get(String.class))) {
            sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        if (!PlayerCache.getToggled().contains(player.getUniqueId())) {
            PlayerCache.getToggled().add(player.getUniqueId());
            sender.sendMessage((SpigotMessages.STAFFCHAT_TOGGLED_OFF.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        PlayerCache.getToggled().remove(player.getUniqueId());
        sender.sendMessage((SpigotMessages.STAFFCHAT_TOGGLED_ON.color()
                .replace("%prefix%", SpigotMessages.PREFIX.color())));
        return false;
    }
}