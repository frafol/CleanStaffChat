package it.frafol.cleanstaffchat.bungee.Listeners;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void handle(PostLoginEvent event){
        if (!(CleanStaffChat.getInstance().getProxy().getPlayers().size() < 1)) {
            ProxiedPlayer player = event.getPlayer();
            if (BungeeConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFF_JOIN_MESSAGE_FORMAT.color()
                                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                                    .replace("%user%", player.getName()))));
                }
            }
        }
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event){
        if (CleanStaffChat.getInstance().getProxy().getPlayers().size() >= 1) {
            ProxiedPlayer player = event.getPlayer();
            if (BungeeConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission
                                            (BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                            .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFF_QUIT_MESSAGE_FORMAT.color()
                                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                                    .replace("%user%", player.getName()))));
                }
            }
        }
    }
}
