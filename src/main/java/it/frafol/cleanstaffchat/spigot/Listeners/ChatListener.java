package it.frafol.cleanstaffchat.spigot.Listeners;

import it.frafol.cleanstaffchat.spigot.CleanStaffChat;
import it.frafol.cleanstaffchat.spigot.enums.SpigotConfig;
import it.frafol.cleanstaffchat.spigot.objects.PlayerCache;
import org.bukkit.Bukkit;
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

        if (Bukkit.getServer().getBukkitVersion().contains("1.19")) {
            PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
            return;
        }

        if (PlayerCache.getToggled_2().contains(event.getPlayer().getUniqueId())) {
            if (PlayerCache.getMuted().contains("true")) {
                PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
                event.setCancelled(true);
                event.getPlayer().sendMessage(SpigotConfig.STAFFCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", SpigotConfig.PREFIX.color()));
                return;
            }
            if (!event.getMessage().startsWith("/")) {
                if (!(SpigotConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                    event.getPlayer().sendMessage((SpigotConfig.MODULE_DISABLED.color()
                            .replace("%prefix%", SpigotConfig.PREFIX.color())
                            .replace("&", "§")));
                } else if (event.getPlayer().hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                    CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                    (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> event.getPlayer().sendMessage(SpigotConfig.STAFFCHAT_FORMAT.color()
                                    .replace("%prefix%", SpigotConfig.PREFIX.color())
                                    .replace("%user%", (event.getPlayer().getName())
                                    .replace("%message%", (event.getMessage())))));
                    event.setCancelled(true);
                } else {
                    PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
                }
            }
        }
    }
}