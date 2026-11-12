package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import de.myzelyam.api.vanish.VanishAPI;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.channel.ChatChannel;
import it.frafol.cleanstaffchat.bukkit.channel.StaffNotifier;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class JoinListener implements Listener {

    private final CleanStaffChat plugin;

    public JoinListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handle(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (plugin.isUpdate() && player.hasPermission(SpigotConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            player.sendMessage(SpigotMessages.UPDATE.color()
                    .replace("%version%", plugin.getUpdatedVersion())
                    .replace("%prefix%", ChatChannel.STAFF.prefix()));
        }

        if (!SpigotConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) return;
        if (!shouldAnnounce(player, SpigotConfig.STAFFCHAT_JOIN_LEAVE_ALL,
                SpigotConfig.STAFFCHAT_JOIN_SILENT_PERMISSION, SpigotConfig.STAFFCHAT_JOIN_SILENT_MODULE)) return;

        StaffNotifier.announce(plugin, player, SpigotMessages.STAFF_JOIN_MESSAGE_FORMAT,
                SpigotMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT, SpigotConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE, false);
    }

    @EventHandler
    public void handle(@NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        PlayerCache.getAfk().remove(player.getUniqueId());

        if (!SpigotConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) return;
        if (!shouldAnnounce(player, SpigotConfig.STAFFCHAT_QUIT_ALL,
                SpigotConfig.STAFFCHAT_QUIT_SILENT_PERMISSION, SpigotConfig.STAFFCHAT_QUIT_SILENT_MODULE)) return;

        StaffNotifier.announce(plugin, player, SpigotMessages.STAFF_QUIT_MESSAGE_FORMAT,
                SpigotMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT, SpigotConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE, false);
    }

    private boolean shouldAnnounce(Player player, SpigotConfig announceAll, SpigotConfig silentPermission, SpigotConfig silentModule) {
        if (!ChatChannel.STAFF.canUse(player) && !announceAll.get(Boolean.class)) return false;
        if (silentModule.get(Boolean.class) && player.hasPermission(silentPermission.get(String.class))) return false;
        return !(plugin.isPremiumVanish() && VanishAPI.getInvisiblePlayers().contains(player.getUniqueId()));
    }
}
