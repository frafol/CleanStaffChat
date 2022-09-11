package it.frafol.cleanstaffchat.velocity.Listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

public class ServerListener {

    public final it.frafol.cleanstaffchat.velocity.CleanStaffChat PLUGIN;

    public ServerListener(it.frafol.cleanstaffchat.velocity.CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void Switch(ServerConnectedEvent event) {
        if (!event.getPreviousServer().isPresent()) {
            return;
        }
        if (!(CleanStaffChat.getInstance().getServer().getAllPlayers().size() < 1)) {
            Player player = event.getPlayer();
            if (VelocityConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) {
                if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    it.frafol.cleanstaffchat.velocity.CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> VelocityConfig.STAFF_SWITCH_MESSAGE_FORMAT.send(players,
                                    new Placeholder("user", player.getUsername()),
                                    new Placeholder("prefix", VelocityConfig.PREFIX.color()),
                                    new Placeholder("server", event.getServer().getServerInfo().getName())));
                }
            }
        }
    }
}