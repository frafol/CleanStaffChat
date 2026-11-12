package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import de.myzelyam.api.vanish.VanishAPI;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.channel.ChatChannel;
import it.frafol.cleanstaffchat.bukkit.channel.StaffNotifier;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class AFKCommand extends CommandBase {

    public AFKCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        final ChatChannel staff = ChatChannel.STAFF;

        if (!SpigotConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {
            staff.reply(sender, SpigotMessages.MODULE_DISABLED);
            return false;
        }

        if (!inGameCheck(sender)) return false;
        final Player player = (Player) sender;

        if (!player.hasPermission(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {
            staff.reply(sender, SpigotMessages.NO_PERMISSION);
            return false;
        }

        final UUID uuid = player.getUniqueId();

        if (PlayerCache.getAfk().add(uuid)) {
            StaffNotifier.announce(plugin, player, SpigotMessages.STAFFCHAT_AFK_ON,
                    SpigotMessages.STAFF_DISCORD_AFK_ON_MESSAGE_FORMAT, SpigotConfig.STAFFCHAT_DISCORD_AFK_MODULE, true);
            return false;
        }

        PlayerCache.getAfk().remove(uuid);
        if (plugin.isPremiumVanish() && VanishAPI.getInvisiblePlayers().contains(uuid)) return false;

        StaffNotifier.announce(plugin, player, SpigotMessages.STAFFCHAT_AFK_OFF,
                SpigotMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT, SpigotConfig.STAFFCHAT_DISCORD_AFK_MODULE, true);
        return false;
    }
}
