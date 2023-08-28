package it.frafol.cleanstaffchat.bukkit.adminchat.commands;

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
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] strings) {

        if (!(SpigotConfig.ADMINCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));
        }

        if (!sender.hasPermission(SpigotConfig.ADMINCHAT_MUTE_PERMISSION.get(String.class))) {
            sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));
            return false;
        }

        if (!PlayerCache.getMuted().contains("true")) {
            PlayerCache.getMuted().add("true");
            sender.sendMessage((SpigotMessages.ADMINCHAT_MUTED.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));

        } else {
            PlayerCache.getMuted().remove("true");
            sender.sendMessage((SpigotMessages.ADMINCHAT_UNMUTED.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())));
        }
        return false;
    }
}
