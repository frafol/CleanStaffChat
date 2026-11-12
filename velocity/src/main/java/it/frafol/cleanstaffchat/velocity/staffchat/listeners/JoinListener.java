package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.channel.ChatChannel;
import it.frafol.cleanstaffchat.velocity.channel.StaffNotifier;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.VanishUtil;
import org.jetbrains.annotations.NotNull;

public class JoinListener {

    private final CleanStaffChat plugin;

    public JoinListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void handle(@NotNull ServerPostConnectEvent event) {
        if (event.getPreviousServer() != null) return;
        final Player player = event.getPlayer();
        if (plugin.isUpdate() && player.hasPermission(VelocityConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            VelocityMessages.UPDATE.send(player,
                    new Placeholder("version", plugin.getUpdatedVersion()),
                    new Placeholder("prefix", ChatChannel.STAFF.prefix()));
        }
        if (!VelocityConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) return;
        if (!shouldAnnounce(player, VelocityConfig.STAFFCHAT_JOIN_LEAVE_ALL,
                VelocityConfig.STAFFCHAT_JOIN_SILENT_PERMISSION, VelocityConfig.STAFFCHAT_JOIN_SILENT_MODULE)) return;
        final String server = player.getCurrentServer().isPresent()
                ? player.getCurrentServer().get().getServerInfo().getName() : "";
        StaffNotifier.announce(player, VelocityMessages.STAFF_JOIN_MESSAGE_FORMAT,
                VelocityMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT, VelocityConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE,
                false, null, server);
    }

    @Subscribe
    public void handle(@NotNull DisconnectEvent event) {
        final Player player = event.getPlayer();
        PlayerCache.getAfk().remove(player.getUniqueId());
        if (player.getCurrentServer().isEmpty()) return;
        if (!VelocityConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) return;
        if (!shouldAnnounce(player, VelocityConfig.STAFFCHAT_QUIT_ALL,
                VelocityConfig.STAFFCHAT_QUIT_SILENT_PERMISSION, VelocityConfig.STAFFCHAT_QUIT_SILENT_MODULE)) return;
        StaffNotifier.announce(player, VelocityMessages.STAFF_QUIT_MESSAGE_FORMAT,
                VelocityMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT, VelocityConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE,
                false, null, "");
    }

    private boolean shouldAnnounce(Player player, VelocityConfig announceAll, VelocityConfig silentPermission, VelocityConfig silentModule) {
        if (!ChatChannel.STAFF.canUse(player) && !announceAll.get(Boolean.class)) return false;
        if (silentModule.get(Boolean.class) && player.hasPermission(silentPermission.get(String.class))) return false;
        return !VanishUtil.isVanished(player);
    }
}
