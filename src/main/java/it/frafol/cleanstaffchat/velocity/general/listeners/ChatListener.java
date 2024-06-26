package it.frafol.cleanstaffchat.velocity.general.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatListener extends ListenerAdapter {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChat(PlayerChatEvent event) {

        Player player = event.getPlayer();

        if (!player.getCurrentServer().isPresent()) {
            return;
        }

        RegisteredServer server = player.getCurrentServer().get().getServer();
        if (PlayerCache.getMutedservers().contains(server.getServerInfo().getName()) || PlayerCache.getMutedservers().contains("all")) {

            if (player.hasPermission(VelocityConfig.MUTECHAT_BYPASS_PERMISSION.get(String.class))) {
                return;
            }

            if (event.getMessage().startsWith("/")) {
                return;
            }

            VelocityMessages.STAFFCHAT_MUTED_ERROR.send(player,
                    new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
            if (!VelocityConfig.DOUBLE_MESSAGE.get(Boolean.class)) {
                            event.setResult(PlayerChatEvent.ChatResult.denied());
                        }
                        
        }
    }
}
