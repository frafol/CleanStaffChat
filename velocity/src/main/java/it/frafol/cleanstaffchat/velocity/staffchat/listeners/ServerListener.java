package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.channel.ChatChannel;
import it.frafol.cleanstaffchat.velocity.channel.StaffNotifier;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import it.frafol.cleanstaffchat.velocity.utils.VanishUtil;
import org.jetbrains.annotations.NotNull;

public class ServerListener {

    private final CleanStaffChat plugin;

    public ServerListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onSwitch(@NotNull ServerConnectedEvent event) {
        final Player player = event.getPlayer();
        final String server = event.getServer().getServerInfo().getName();

        if (ChatChannel.isTalkingAnywhere(player.getUniqueId())) ChatUtil.sendChannelMessage(player, true);
        final boolean vanished = VanishUtil.isVanished(player);
        if (VelocityConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)
                && PlayerCache.getAfk().contains(player.getUniqueId())
                && player.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {
            PlayerCache.getAfk().remove(player.getUniqueId());
            if (!vanished) {
                StaffNotifier.announce(player, VelocityMessages.STAFFCHAT_AFK_OFF,
                        VelocityMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT, VelocityConfig.STAFFCHAT_DISCORD_AFK_MODULE,
                        true, null, server);
            }
        }

        if (plugin.getServer().getAllPlayers().isEmpty()) return;
        if (!VelocityConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) return;
        if (event.getPreviousServer().isEmpty()) return;
        if (!ChatChannel.STAFF.canUse(player) && !VelocityConfig.STAFFCHAT_SWITCH_ALL.get(Boolean.class)) return;
        if (VelocityConfig.STAFFCHAT_SWITCH_SILENT_MODULE.get(Boolean.class)
                && player.hasPermission(VelocityConfig.STAFFCHAT_SWITCH_SILENT_PERMISSION.get(String.class))) return;
        if (vanished) return;
        StaffNotifier.announce(player, VelocityMessages.STAFF_SWITCH_MESSAGE_FORMAT,
                VelocityMessages.STAFF_DISCORD_SWITCH_MESSAGE_FORMAT, VelocityConfig.STAFFCHAT_DISCORD_SWITCH_MODULE,
                false, event.getPreviousServer().get().getServerInfo().getName(), server);
    }
}
