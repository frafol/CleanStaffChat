package it.frafol.cleanstaffchat.velocity.utils;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SwitchUtil extends ListenerAdapter {

    public final CleanStaffChat PLUGIN;

    public SwitchUtil(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void onSwitch(ServerPostConnectEvent event) {
        if (toggled(event.getPlayer())) {
            ChatUtil.sendChannelMessage(event.getPlayer(), true);
        }
    }

    private boolean toggled(Player player) {
        return PlayerCache.getToggled_2().contains(player.getUniqueId()) ||
                PlayerCache.getToggled_2_admin().contains(player.getUniqueId()) ||
                PlayerCache.getToggled_2_donor().contains(player.getUniqueId());
    }
}
