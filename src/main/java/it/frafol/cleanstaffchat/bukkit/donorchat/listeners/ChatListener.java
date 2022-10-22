package it.frafol.cleanstaffchat.bukkit.donorchat.listeners;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        if (PlayerCache.getToggled_2_donor().contains(event.getPlayer().getUniqueId())) {

            if (PlayerCache.getMuted().contains("true")) {

                PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());

                event.setCancelled(true);

                event.getPlayer().sendMessage(SpigotConfig.DONORCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", SpigotConfig.DONORPREFIX.color()));

                return;

            }

            if (PlayerCache.getCooldown().contains(event.getPlayer().getUniqueId())) {

                PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());

                event.setCancelled(true);

                event.getPlayer().sendMessage(SpigotConfig.DONORCHAT_COOLDOWN_MESSAGE.color()
                        .replace("%prefix%", SpigotConfig.DONORPREFIX.color()));

                return;

            }

            if (!event.getMessage().startsWith("/")) {

                if (!(SpigotConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {

                    event.getPlayer().sendMessage((SpigotConfig.MODULE_DISABLED.color()
                            .replace("%prefix%", SpigotConfig.DONORPREFIX.color())
                            .replace("&", "§")));

                } else if (event.getPlayer().hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {

                    final String message = event.getMessage();

                    if (SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {

                        if (message.contains("&0") ||
                                message.contains("&1") ||
                                message.contains("&2") ||
                                message.contains("&3") ||
                                message.contains("&4") ||
                                message.contains("&5") ||
                                message.contains("&6") ||
                                message.contains("&7") ||
                                message.contains("&8") ||
                                message.contains("&9") ||
                                message.contains("&a") ||
                                message.contains("&b") ||
                                message.contains("&c") ||
                                message.contains("&d") ||
                                message.contains("&e") ||
                                message.contains("&f") ||
                                message.contains("&k") ||
                                message.contains("&l") ||
                                message.contains("&m") ||
                                message.contains("&n") ||
                                message.contains("&o") ||
                                message.contains("&r")) {

                            event.getPlayer().sendMessage(SpigotConfig.COLOR_CODES.color()
                                    .replace("%prefix%", SpigotConfig.DONORPREFIX.color())
                                    .replace("&", "§"));

                            event.setCancelled(true);

                            return;

                        }
                    }

                    PlayerCache.getCooldown().add(event.getPlayer().getUniqueId());

                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            PlayerCache.getCooldown().remove(event.getPlayer().getUniqueId());
                            cancel();

                        }

                    }.runTaskTimer(PLUGIN, Math.multiplyExact(SpigotConfig.DONOR_TIMER.get(Integer.class), 20), 1);

                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotConfig.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotConfig.DONORPREFIX.color())
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%message%", event.getMessage())
                                        .replace("%displayname%", user_prefix + event.getPlayer().getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%server%", "")
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("&", "§")));

                    } else {

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotConfig.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotConfig.DONORPREFIX.color())
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%message%", event.getMessage())
                                        .replace("%server%", "")
                                        .replace("&", "§")));

                    }

                    event.setCancelled(true);

                } else {

                    PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());

                }
            }
        }
    }
}