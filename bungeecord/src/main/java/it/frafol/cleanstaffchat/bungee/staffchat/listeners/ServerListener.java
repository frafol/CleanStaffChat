package it.frafol.cleanstaffchat.bungee.staffchat.listeners;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.channel.ChatChannel;
import it.frafol.cleanstaffchat.bungee.channel.StaffNotifier;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class ServerListener implements Listener {

    private final CleanStaffChat plugin;

    public ServerListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwitch(@NotNull ServerSwitchEvent event) {
        if (event.getFrom() == null) return;
        final ProxiedPlayer player = event.getPlayer();

        if (ChatChannel.isTalkingAnywhere(player.getUniqueId())) {
            PlayerCache.sendChannelMessage(player, true);
        }

        final boolean vanished = plugin.isPremiumVanish() && BungeeVanishAPI.getInvisiblePlayers().contains(player.getUniqueId());

        if (BungeeConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)
                && PlayerCache.getAfk().contains(player.getUniqueId())
                && player.hasPermission(BungeeConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

            PlayerCache.getAfk().remove(player.getUniqueId());
            if (vanished) return;

            StaffNotifier.announce(player, BungeeMessages.STAFFCHAT_AFK_OFF,
                    BungeeMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT, BungeeConfig.STAFFCHAT_DISCORD_AFK_MODULE, true, null);
        }

        if (!BungeeConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) return;
        if (!ChatChannel.STAFF.canUse(player) && !BungeeConfig.STAFFCHAT_SWITCH_ALL.get(Boolean.class)) return;
        if (BungeeConfig.STAFFCHAT_SWITCH_SILENT_MODULE.get(Boolean.class)
                && player.hasPermission(BungeeConfig.STAFFCHAT_SWITCH_SILENT_PERMISSION.get(String.class))) return;
        if (vanished) return;

        StaffNotifier.announce(player, BungeeMessages.STAFF_SWITCH_MESSAGE_FORMAT,
                BungeeMessages.STAFF_DISCORD_SWITCH_MESSAGE_FORMAT, BungeeConfig.STAFFCHAT_DISCORD_SWITCH_MODULE,
                false, event.getFrom().getName());
    }
}
