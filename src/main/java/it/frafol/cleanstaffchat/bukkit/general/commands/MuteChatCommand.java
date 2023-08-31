package it.frafol.cleanstaffchat.bukkit.general.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MuteChatCommand extends CommandBase {

    public MuteChatCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSource, @NotNull String commandLabel, @NotNull String[] args) {

        if (args.length == 0) {

            if (!commandSource.hasPermission(SpigotConfig.MUTECHAT_ALL_PERMISSION.get(String.class)) &&
                    !commandSource.hasPermission(SpigotConfig.MUTECHAT_PERMISSION.get(String.class))) {
                commandSource.sendMessage((SpigotMessages.NO_PERMISSION.color()
                        .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())));
                return false;
            }

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                commandSource.sendMessage((SpigotMessages.MUTECHAT_DISABLED.color()
                        .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())));
                return false;
            }

            PlayerCache.getMutedservers().add("all");
            commandSource.sendMessage((SpigotMessages.MUTECHAT_ENABLED.color()
                    .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())));
        } else {
            commandSource.sendMessage((SpigotMessages.MUTECHAT_USAGE.color()
                    .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())));
        }
        return false;
    }
}
