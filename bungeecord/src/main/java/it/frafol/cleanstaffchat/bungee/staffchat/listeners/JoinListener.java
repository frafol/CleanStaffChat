package it.frafol.cleanstaffchat.bungee.staffchat.listeners;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.channel.ChatChannel;
import it.frafol.cleanstaffchat.bungee.channel.StaffNotifier;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class JoinListener implements Listener {

    private final CleanStaffChat plugin;

    public JoinListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    /** First server switch of a session = the player joined the network. */
    @EventHandler
    public void handle(@NotNull ServerSwitchEvent event) {
        if (event.getFrom() != null) return;
        final ProxiedPlayer player = event.getPlayer();

        if (plugin.isUpdate() && player.hasPermission(BungeeConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            player.sendMessage(TextComponent.fromLegacy(BungeeMessages.UPDATE.color()
                    .replace("%version%", plugin.getUpdatedVersion())
                    .replace("%prefix%", ChatChannel.STAFF.prefix())));
        }

        if (!BungeeConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) return;
        if (!shouldAnnounce(player, BungeeConfig.STAFFCHAT_JOIN_LEAVE_ALL,
                BungeeConfig.STAFFCHAT_JOIN_SILENT_PERMISSION, BungeeConfig.STAFFCHAT_JOIN_SILENT_MODULE)) return;

        StaffNotifier.announce(player, BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT,
                BungeeMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT, BungeeConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE, false, null);
    }

    @EventHandler
    public void handle(@NotNull PlayerDisconnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        PlayerCache.getAfk().remove(player.getUniqueId());

        if (player.getServer() == null) return;
        if (!BungeeConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) return;
        if (!shouldAnnounce(player, BungeeConfig.STAFFCHAT_QUIT_ALL,
                BungeeConfig.STAFFCHAT_QUIT_SILENT_PERMISSION, BungeeConfig.STAFFCHAT_QUIT_SILENT_MODULE)) return;

        StaffNotifier.announce(player, BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT,
                BungeeMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT, BungeeConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE, false, null);
    }

    private boolean shouldAnnounce(ProxiedPlayer player, BungeeConfig announceAll, BungeeConfig silentPermission, BungeeConfig silentModule) {
        if (!ChatChannel.STAFF.canUse(player) && !announceAll.get(Boolean.class)) return false;
        if (silentModule.get(Boolean.class) && player.hasPermission(silentPermission.get(String.class))) return false;
        return !(plugin.isPremiumVanish() && BungeeVanishAPI.getInvisiblePlayers().contains(player.getUniqueId()));
    }
}
