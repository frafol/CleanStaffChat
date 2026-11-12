package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.channel.ChatChannel;
import it.frafol.cleanstaffchat.velocity.channel.StaffNotifier;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.VanishUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AFKCommand implements SimpleCommand {

    @Override
    public void execute(@NotNull Invocation invocation) {
        final CommandSource source = invocation.source();
        final ChatChannel staff = ChatChannel.STAFF;

        if (!VelocityConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {
            staff.reply(source, VelocityMessages.MODULE_DISABLED);
            return;
        }

        if (!(source instanceof Player player)) {
            staff.reply(source, VelocityMessages.PLAYER_ONLY);
            return;
        }

        if (!player.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {
            staff.reply(source, VelocityMessages.NO_PERMISSION);
            return;
        }

        final UUID uuid = player.getUniqueId();
        final String server = player.getCurrentServer().isPresent()
                ? player.getCurrentServer().get().getServerInfo().getName() : "";
        if (PlayerCache.getAfk().add(uuid)) {
            StaffNotifier.announce(player, VelocityMessages.STAFFCHAT_AFK_ON,
                    VelocityMessages.STAFF_DISCORD_AFK_ON_MESSAGE_FORMAT, VelocityConfig.STAFFCHAT_DISCORD_AFK_MODULE,
                    true, null, server);
            return;
        }

        PlayerCache.getAfk().remove(uuid);
        if (VanishUtil.isVanished(player)) return;
        StaffNotifier.announce(player, VelocityMessages.STAFFCHAT_AFK_OFF,
                VelocityMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT, VelocityConfig.STAFFCHAT_DISCORD_AFK_MODULE,
                true, null, server);
    }
}
