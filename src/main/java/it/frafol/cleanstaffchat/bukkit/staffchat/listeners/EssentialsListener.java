package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EssentialsListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public EssentialsListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onAFKChange(AfkStatusChangeEvent event) {

        if (!event.getValue()) {

            if (!PlayerCache.getAfk().contains(event.getAffected().getUUID())) {

                return;

            }

            if (!event.getAffected().isPermissionSet(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

                return;

            }

            if (!SpigotConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)) {

                return;

            }

            if (PlayerCache.getAfk().contains(event.getAffected().getUUID())) {

                if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User user = api.getUserManager().getUser(event.getAffected().getUUID());

                    assert user != null;
                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();
                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                    (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_OFF.color()
                                    .replace("%prefix%", SpigotConfig.PREFIX.color())
                                    .replace("%user%", event.getAffected().getName())
                                    .replace("%displayname%", user_prefix + event.getAffected().getName() + user_suffix)
                                    .replace("%userprefix%", user_prefix)
                                    .replace("%usersuffix%", user_suffix)));

                } else {

                    CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                    (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_OFF.color()
                                    .replace("%prefix%", SpigotConfig.PREFIX.color())
                                    .replace("%user%", event.getAffected().getName())
                                    .replace("%userprefix%", "")
                                    .replace("%usersuffix%", "")
                                    .replace("%displayname%", event.getAffected().getName())));

                }

                PlayerCache.getAfk().remove(event.getAffected().getUUID());

            }
        } else {
            if (!PlayerCache.getAfk().contains(event.getAffected().getUUID())) {

                return;

            }

            if (!event.getAffected().isPermissionSet(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

                return;

            }

            if (!SpigotConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)) {

                return;

            }

            if (PlayerCache.getAfk().contains(event.getAffected().getUUID())) {

                if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User user = api.getUserManager().getUser(event.getAffected().getUUID());

                    assert user != null;
                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();
                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                    (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_ON.color()
                                    .replace("%prefix%", SpigotConfig.PREFIX.color())
                                    .replace("%user%", event.getAffected().getName())
                                    .replace("%displayname%", user_prefix + event.getAffected().getName() + user_suffix)
                                    .replace("%userprefix%", user_prefix)
                                    .replace("%usersuffix%", user_suffix)));

                } else {

                    CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                    (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(SpigotConfig.STAFFCHAT_AFK_ON.color()
                                    .replace("%prefix%", SpigotConfig.PREFIX.color())
                                    .replace("%user%", event.getAffected().getName())
                                    .replace("%userprefix%", "")
                                    .replace("%usersuffix%", "")
                                    .replace("%displayname%", event.getAffected().getName())));

                }

                PlayerCache.getAfk().add(event.getAffected().getUUID());

            }
        }
    }
}
