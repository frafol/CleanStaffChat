package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import de.myzelyam.api.vanish.VanishAPI;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.channel.StaffNotifier;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Removes the AFK status of a staff member as soon as they move.
 */
public class MoveListener implements Listener {

    private final CleanStaffChat plugin;

    public MoveListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (!PlayerCache.getAfk().contains(player.getUniqueId())) return;
        if (!player.hasPermission(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) return;
        if (!SpigotConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)) return;
        PlayerCache.getAfk().remove(player.getUniqueId());
        if (plugin.isPremiumVanish() && VanishAPI.getInvisiblePlayers().contains(player.getUniqueId())) return;
        StaffNotifier.announce(plugin, player, SpigotMessages.STAFFCHAT_AFK_OFF,
                SpigotMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT, SpigotConfig.STAFFCHAT_DISCORD_AFK_MODULE, true);
    }
}
