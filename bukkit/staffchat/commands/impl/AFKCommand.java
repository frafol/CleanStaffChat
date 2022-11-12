package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class AFKCommand extends CommandBase {

    public AFKCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        
        if (!SpigotConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {

            sender.sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));

            return false;
        }

        if (!inGameCheck(sender)) {
            return false;
        }
        
        Player player = (Player) sender;
        if (!player.hasPermission(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

            player.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));

            return false;

        }

        if (!PlayerCache.getAfk().contains(player.getUniqueId())) {
            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(player.getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_ON.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                .replace("%userprefix%", user_prefix)
                                .replace("%server%", "")
                                .replace("%usersuffix%", user_suffix)));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_ON.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%userprefix%", "")
                                .replace("%server%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", player.getName())));

            }

            PlayerCache.getAfk().add(player.getUniqueId());

        } else {

            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(player.getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                .replace("%userprefix%", user_prefix)
                                .replace("%server%", "")
                                .replace("%usersuffix%", user_suffix)));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%userprefix%", "")
                                .replace("%server%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", player.getName())));

            }

            PlayerCache.getAfk().remove(player.getUniqueId());

        }
        return false;
    }
}
