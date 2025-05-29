package it.frafol.cleanstaffchat.bungee.objects;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class SwitchUtil implements Listener {

    public final CleanStaffChat PLUGIN;

    public SwitchUtil(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        if (toggled(event.getPlayer())) {
            PlayerCache.sendChannelMessage(event.getPlayer(), true);
        }
    }

    private boolean toggled(ProxiedPlayer player) {
        return PlayerCache.getToggled_2().contains(player.getUniqueId()) ||
                PlayerCache.getToggled_2_admin().contains(player.getUniqueId()) ||
                PlayerCache.getToggled_2_donor().contains(player.getUniqueId());
    }
}
