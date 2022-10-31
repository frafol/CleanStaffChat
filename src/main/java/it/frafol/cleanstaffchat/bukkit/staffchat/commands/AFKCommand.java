package it.frafol.cleanstaffchat.bukkit.staffchat.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AFKCommand implements CommandExecutor {

    public final CleanStaffChat PLUGIN;

    public AFKCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] strings) {

        if (!(command.getName().equalsIgnoreCase("scafk")
                || command.getName().equalsIgnoreCase("staffchatafk")
                || command.getName().equalsIgnoreCase("cleanscafk")
                || command.getName().equalsIgnoreCase("cleanstaffchatafk")
                || command.getName().equalsIgnoreCase("staffafk"))) {

            return false;

        }

        if (!(sender instanceof Player)) {

            sender.sendMessage((SpigotConfig.PLAYER_ONLY.color()
                    .replace("%prefix%", SpigotConfig.PREFIX.color())));

            return false;

        }

        if (!SpigotConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {

            sender.sendMessage((SpigotConfig.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotConfig.PREFIX.color())));

            return false;

        }

        if (!sender.hasPermission(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

            sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotConfig.PREFIX.color())));

            return false;

        }

        if (!PlayerCache.getAfk().contains(((Player) sender).getUniqueId())) {
            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((Player) sender).getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_ON.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%displayname%", user_prefix + sender.getName() + user_suffix)
                                .replace("%userprefix%", user_prefix)
                                .replace("%server%", "")
                                .replace("%usersuffix%", user_suffix)));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_ON.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%userprefix%", "")
                                .replace("%server%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", sender.getName())));

            }

            PlayerCache.getAfk().add(((Player) sender).getUniqueId());

            if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") != null) {

                

            }

        } else {

            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((Player) sender).getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%displayname%", user_prefix + sender.getName() + user_suffix)
                                .replace("%userprefix%", user_prefix)
                                .replace("%server%", "")
                                .replace("%usersuffix%", user_suffix)));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%userprefix%", "")
                                .replace("%server%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", sender.getName())));

            }

            PlayerCache.getAfk().remove(((Player) sender).getUniqueId());

        }

        return false;

    }
}
