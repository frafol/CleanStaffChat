package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StaffListCommand extends CommandBase {

    public StaffListCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {

        if (!sender.hasPermission(SpigotConfig.STAFFLIST_PERMISSION.get(String.class))) {
            return false;
        }

        if (args.length == 0) {

            LuckPerms api = LuckPermsProvider.get();

            sender.sendMessage(SpigotMessages.LIST_HEADER.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));

            String user_prefix;

            for (Player players : plugin.getServer().getOnlinePlayers()) {

                if (players.hasPermission(SpigotConfig.STAFFLIST_PERMISSION.get(String.class))) {

                    User user = api.getUserManager().getUser(players.getUniqueId());

                    if (user == null) {
                        continue;
                    }

                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

                    if (group == null || group.getDisplayName() == null) {

                        if (prefix != null) {
                            user_prefix = prefix;
                        } else {
                            user_prefix = "";
                        }

                        if (players.getServer() == null) {
                            continue;
                        }

                        sender.sendMessage(SpigotMessages.LIST_FORMAT.color()
                                .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                .replace("%player%", players.getName())
                                .replace("%server%", "")
                                .replace("%prefix%", SpigotMessages.PREFIX.color()));

                        continue;
                    }

                    user_prefix = prefix == null ? group.getDisplayName() : prefix;

                    if (players.getServer() == null) {
                        continue;
                    }

                    sender.sendMessage(SpigotMessages.LIST_FORMAT.color()
                            .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                            .replace("%player%", players.getName())
                            .replace("%server%", "")
                            .replace("%prefix%", SpigotMessages.PREFIX.color()));

                }
            }
            sender.sendMessage(SpigotMessages.LIST_FOOTER.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));
        }
        return false;
    }
}
