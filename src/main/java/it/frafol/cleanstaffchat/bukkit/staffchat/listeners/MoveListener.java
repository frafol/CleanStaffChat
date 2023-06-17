package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class MoveListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public MoveListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {

        final Player player = event.getPlayer();

        if (!PlayerCache.getAfk().contains(player.getUniqueId())) {

            return;

        }

        if (!player.hasPermission(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

            return;

        }

        if (!SpigotConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)) {

            return;

        }

        if (PlayerCache.getAfk().contains(player.getUniqueId())) {

            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(player.getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", PlayerCache.translateHex(user_prefix) + player.getName() + PlayerCache.translateHex(user_suffix))
                                .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", player.getName())));

            }

            PlayerCache.getAfk().remove(player.getUniqueId());

        }
    }
}
