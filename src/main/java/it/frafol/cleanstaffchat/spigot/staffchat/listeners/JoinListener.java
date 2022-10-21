package it.frafol.cleanstaffchat.spigot.staffchat.listeners;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.UpdateCheck;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                && (SpigotConfig.UPDATE_CHECK.get(Boolean.class))) {
            new UpdateCheck(PLUGIN).getVersion(version -> {
                if (!PLUGIN.getDescription().getVersion().equals(version)) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz");
                    PLUGIN.getLogger().warning("There is a new update available, download it on SpigotMC!");
                }
            });
        }
        if (!(CleanStaffChat.getInstance().getServer().getOnlinePlayers().size() < 1)) {
            Player player = event.getPlayer();
            if (SpigotConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || SpigotConfig.STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {
                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {
                            if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_JOIN_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                                .replace("%userprefix%", user_prefix)
                                                .replace("%usersuffix%", user_suffix)
                                                .replace("%user%", player.getName())));
                            }
                        }
                    } else {
                        for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {
                            if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_JOIN_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%user%", player.getName())));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void handle(PlayerQuitEvent event){
        if (!(CleanStaffChat.getInstance().getServer().getOnlinePlayers().size() < 1)) {
            Player player = event.getPlayer();
            if (SpigotConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || SpigotConfig.STAFFCHAT_QUIT_ALL.get(Boolean.class)) {
                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                        assert user != null;

                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {
                            if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_QUIT_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                                .replace("%userprefix%", user_prefix)
                                                .replace("%usersuffix%", user_suffix)
                                                .replace("%user%", player.getName())));
                            }
                        }
                    } else {
                        for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {
                            if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_QUIT_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%user%", player.getName())));
                            }
                        }
                    }
                }
            }
        }

        PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

    }
}
