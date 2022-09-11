package it.frafol.cleanstaffchat.bungee.Listeners;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {
    public final CleanStaffChat PLUGIN;

    public ServerListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void Switch(ServerSwitchEvent event){
        if (!(CleanStaffChat.getInstance().getProxy().getPlayers().size() < 1)) {
            ProxiedPlayer player = event.getPlayer();
            if (BungeeConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) {
                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFF_SWITCH_MESSAGE_FORMAT.color()
                                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                                    .replace("%user%", player.getName())
                                    .replace("%server%", player.getServer().getInfo().getName()))));
                }
            }
        }
    }
}
