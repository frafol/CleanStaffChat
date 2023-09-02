package it.frafol.cleanstaffchat.bukkit.general.listeners;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        if (PlayerCache.getMutedservers().contains("all")) {

            if (player.hasPermission(SpigotConfig.MUTECHAT_BYPASS_PERMISSION.get(String.class))) {
                return;
            }

            if (event.getMessage().startsWith("/")) {
                return;
            }

            player.sendMessage((SpigotMessages.STAFFCHAT_MUTED_ERROR.color()
                    .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())));
            event.setCancelled(true);
        }
    }
}
