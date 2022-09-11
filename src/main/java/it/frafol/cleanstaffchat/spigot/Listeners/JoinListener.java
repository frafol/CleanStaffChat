package it.frafol.cleanstaffchat.spigot.Listeners;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
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
        if (!(CleanStaffChat.getInstance().getServer().getOnlinePlayers().size() < 1)) {
            Player player = event.getPlayer();
            if (SpigotConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    for(Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {
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

    @EventHandler
    public void handle(PlayerQuitEvent event){
        if (CleanStaffChat.getInstance().getServer().getOnlinePlayers().size() >= 1) {
            Player player = event.getPlayer();
            if (SpigotConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    for(Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {
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
}
