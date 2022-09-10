package it.frafol.cleanstaffchat.bungee.Listeners;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(ChatEvent event) {

        String message = event.getMessage();

        if (PlayerCache.getToggled_2().contains(((ProxiedPlayer) event.getSender()).getUniqueId())) {
            if (PlayerCache.getMuted().contains("true")) {
                PlayerCache.getToggled_2().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
                event.setCancelled(true);
                ((ProxiedPlayer)event.getSender()).sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                        .replace("&", "§")));
                return;
            }
            if (!event.getMessage().startsWith("/")) {
                if (!(BungeeConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                    ((ProxiedPlayer)event.getSender()).sendMessage(new TextComponent(BungeeConfig.MODULE_DISABLED.color()
                            .replace("%prefix%", BungeeConfig.PREFIX.color())
                            .replace("&", "§")));
                } else if (((ProxiedPlayer) event.getSender()).hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    event.setCancelled(true);
                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> ((ProxiedPlayer)event.getSender()).sendMessage(new TextComponent(BungeeConfig.STAFFCHAT_FORMAT.color()
                                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                                    .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                    .replace("%message%", message)
                                    .replace("&", "§"))));
                } else {
                    PlayerCache.getToggled_2().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
                }
            }
        }
    }
}