package it.frafol.cleanstaffchat.Listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import it.frafol.cleanstaffchat.CleanStaffChat;
import it.frafol.cleanstaffchat.enums.VelocityConfig;
import it.frafol.cleanstaffchat.objects.Placeholder;
import it.frafol.cleanstaffchat.objects.PlayerCache;

import static it.frafol.cleanstaffchat.enums.VelocityConfig.*;

public class ChatListener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChat(PlayerChatEvent event) {

        String message = event.getMessage();
        String sender = event.getPlayer().getUsername();

        if (!(STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
            MODULE_DISABLED.send(event.getPlayer(), new Placeholder("prefix", PREFIX.color()));
            return;
        }

        if (PlayerCache.getToggled_2().contains(event.getPlayer().getUniqueId())) {
            if (event.getPlayer().hasPermission(STAFFCHAT_USE_PERMISSION.get(String.class))) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
                CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                new Placeholder("user", sender),
                                new Placeholder("message", message),
                                new Placeholder("prefix", PREFIX.color())));

            } else {
                PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
            }
        } else {
            STAFFCHAT_MUTED_ERROR.send(event.getPlayer(),
                    new Placeholder("prefix", PREFIX.color()));
        }
    }
}