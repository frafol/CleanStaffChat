package it.frafol.cleanstaffchat.bukkit.adminchat.commands;

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
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] strings) {

        if (!(SpigotConfig.ADMINCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            sender.sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));
            return false;
        }

        if (!inGameCheck(sender, SpigotMessages.ADMINARGUMENTS.color().replace("%prefix%", SpigotMessages.PREFIX.color()))) {
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(SpigotConfig.ADMINCHAT_TOGGLE_PERMISSION.get(String.class))) {
            sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));
            return false;
        }

        if (!PlayerCache.getToggled_admin().contains(player.getUniqueId())) {
            PlayerCache.getToggled_admin().add(player.getUniqueId());
            sender.sendMessage((SpigotMessages.ADMINCHAT_TOGGLED_OFF.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));
            return false;
        }

        PlayerCache.getToggled_admin().remove(player.getUniqueId());
        sender.sendMessage((SpigotMessages.ADMINCHAT_TOGGLED_ON.color()
                .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));
        return false;
    }
}