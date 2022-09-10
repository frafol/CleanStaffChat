package it.frafol.cleanstaffchat.velocity.Listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class ChatListener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChat(PlayerChatEvent event) {

        String message = event.getMessage();
        String sender = event.getPlayer().getUsername();

        if (PlayerCache.getToggled_2().contains(event.getPlayer().getUniqueId())) {
            if (event.getPlayer().hasPermission(STAFFCHAT_USE_PERMISSION.get(String.class))) {
                if (!(STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                    MODULE_DISABLED.send(event.getPlayer(), new Placeholder("prefix", PREFIX.color()));
                    return;
                }
                if (!event.getMessage().startsWith("/")) {
                    if (!PlayerCache.getMuted().contains("true")) {
                        event.setResult(PlayerChatEvent.ChatResult.denied());
                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("prefix", PREFIX.color())));
                    } else {
                        STAFFCHAT_MUTED_ERROR.send(event.getPlayer(),
                                new Placeholder("prefix", PREFIX.color()));
                    }
                }
            } else {
                PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
            }
        }
    }
}