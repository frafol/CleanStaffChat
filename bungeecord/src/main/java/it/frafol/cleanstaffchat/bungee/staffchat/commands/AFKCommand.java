package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.channel.ChatChannel;
import it.frafol.cleanstaffchat.bungee.channel.StaffNotifier;
import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class AFKCommand extends Command {

    public AFKCommand() {
        super(BungeeCommandsConfig.STAFFCHAT_AFK.getStringList().get(0), "",
                BungeeCommandsConfig.STAFFCHAT_AFK.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        final ChatChannel staff = ChatChannel.STAFF;

        if (!(sender instanceof ProxiedPlayer player)) {
            staff.reply(sender, BungeeMessages.PLAYER_ONLY);
            return;
        }

        if (!BungeeConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {
            staff.reply(sender, BungeeMessages.MODULE_DISABLED);
            return;
        }

        if (!sender.hasPermission(BungeeConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {
            staff.reply(sender, BungeeMessages.NO_PERMISSION);
            return;
        }

        final UUID uuid = player.getUniqueId();

        if (PlayerCache.getAfk().add(uuid)) {
            StaffNotifier.announce(player, BungeeMessages.STAFFCHAT_AFK_ON,
                    BungeeMessages.STAFF_DISCORD_AFK_ON_MESSAGE_FORMAT, BungeeConfig.STAFFCHAT_DISCORD_AFK_MODULE, true, null);
            return;
        }

        PlayerCache.getAfk().remove(uuid);
        if (CleanStaffChat.getInstance().isPremiumVanish() && BungeeVanishAPI.getInvisiblePlayers().contains(uuid)) return;

        StaffNotifier.announce(player, BungeeMessages.STAFFCHAT_AFK_OFF,
                BungeeMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT, BungeeConfig.STAFFCHAT_DISCORD_AFK_MODULE, true, null);
    }
}
