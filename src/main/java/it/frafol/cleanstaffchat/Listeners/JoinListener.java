package it.frafol.cleanstaffchat.Listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.CleanStaffChat;
import it.frafol.cleanstaffchat.objects.Placeholder;
import it.frafol.cleanstaffchat.enums.VelocityConfig;

import static it.frafol.cleanstaffchat.enums.VelocityConfig.*;

public class JoinListener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void handle(LoginEvent event){
        if (!(CleanStaffChat.getInstance().getServer().getAllPlayers().size() < 1)) {
            Player player = event.getPlayer();
            if (STAFF_JOIN_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    for(Player all : CleanStaffChat.getInstance().getServer().getAllPlayers()) {
                        if (all.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission
                                            (VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                    .forEach(players -> STAFF_JOIN_MESSAGE_FORMAT.send(players,
                                            new Placeholder("user", player.getUsername()),
                                            new Placeholder("prefix", PREFIX.color())));
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void handle(DisconnectEvent event){
        if (CleanStaffChat.getInstance().getServer().getAllPlayers().size() >= 1) {
            Player player = event.getPlayer();
            if (STAFF_QUIT_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    for(Player all : CleanStaffChat.getInstance().getServer().getAllPlayers()) {
                        if (all.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission
                                            (VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                    .forEach(players -> STAFF_QUIT_MESSAGE_FORMAT.send(players,
                                            new Placeholder("user", player.getUsername()),
                                            new Placeholder("prefix", PREFIX.color())));
                        }
                    }
                }
            }
        }
    }
}
