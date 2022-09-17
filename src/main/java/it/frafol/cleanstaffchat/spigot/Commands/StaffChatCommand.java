package it.frafol.cleanstaffchat.spigot.Commands;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class StaffChatCommand implements CommandExecutor {

    public final CleanStaffChat plugin;

    public StaffChatCommand(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(org.bukkit.command.@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] strings) {

        if (strings.length == 0) {

            if (!(sender instanceof Player)) {
                sender.sendMessage((SpigotConfig.ARGUMENTS.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color())));
                return false;
            }

            Player player = (Player) sender;

            if (sender.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                if (!PlayerCache.getToggled_2().contains(player.getUniqueId())) {
                    if (!(SpigotConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                        sender.sendMessage((SpigotConfig.MODULE_DISABLED.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())));
                        return false;
                    }
                    if (!PlayerCache.getMuted().contains("true")) {
                        PlayerCache.getToggled_2().add(player.getUniqueId());
                        sender.sendMessage((SpigotConfig.STAFFCHAT_TALK_ENABLED.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())));
                    } else {
                        sender.sendMessage((SpigotConfig.ARGUMENTS.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())));
                        return false;
                    }
                    return false;
                } else if (PlayerCache.getToggled_2().contains(player.getUniqueId())) {
                    PlayerCache.getToggled_2().remove(player.getUniqueId());
                    sender.sendMessage((SpigotConfig.STAFFCHAT_TALK_DISABLED.color()
                            .replace("%prefix%", SpigotConfig.PREFIX.color())));
                    return false;
                }
            } else {
                sender.sendMessage(("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));
                return false;
            }
            return false;
        }

        String message = String.join(" ", Arrays.copyOfRange(strings, 0, strings.length));

        String commandsender = !(sender instanceof Player) ? SpigotConfig.CONSOLE_PREFIX.get(String.class) :
                sender.getName();

        if (sender.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                if (sender instanceof Player) {
                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(((Player) sender).getUniqueId());
                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;
                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotConfig.PREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%displayname%", user_prefix + commandsender + user_suffix)
                                        .replace("%message%", message)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("&", "§")));
                    } else {
                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotConfig.PREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%message%", message)
                                        .replace("&", "§")));
                    }
                } else if (SpigotConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {
                    if (!PlayerCache.getMuted().contains("true")) {
                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage((SpigotConfig.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotConfig.PREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%message%", message))));
                    } else {
                        sender.sendMessage((SpigotConfig.STAFFCHAT_MUTED_ERROR.color()
                                .replace("%prefix%", SpigotConfig.PREFIX.color())));
                        return false;
                    }
                    sender.sendMessage((SpigotConfig.STAFFCHAT_FORMAT.color()
                            .replace("%prefix%", SpigotConfig.PREFIX.color())
                            .replace("%user%", commandsender)
                            .replace("%message%", message)));
                    return false;
                } else {
                    sender.sendMessage((SpigotConfig.PLAYER_ONLY.color()
                            .replace("%prefix%", SpigotConfig.PREFIX.color())));
                }
                return false;
            } else {

                sender.sendMessage((SpigotConfig.STAFFCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color())));

            }
            return false;
        } else {
            sender.sendMessage((SpigotConfig.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotConfig.PREFIX.color())));
        }
        return false;
    }
}