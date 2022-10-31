package it.frafol.cleanstaffchat.bukkit.adminchat.listeners;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        if (PlayerCache.getToggled_2_admin().contains(event.getPlayer().getUniqueId())) {

            if (PlayerCache.getMuted().contains("true")) {

                PlayerCache.getToggled_2_admin().remove(event.getPlayer().getUniqueId());

                event.setCancelled(true);

                event.getPlayer().sendMessage(SpigotConfig.ADMINCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", SpigotConfig.ADMINPREFIX.color()));

                return;

            }

            if (!event.getMessage().startsWith("/")) {

                if (!(SpigotConfig.ADMINCHAT_TALK_MODULE.get(Boolean.class))) {

                    event.getPlayer().sendMessage((SpigotConfig.MODULE_DISABLED.color()
                            .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())
                            .replace("&", "§")));

                } else if (event.getPlayer().hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) {

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
                                    .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())
                                    .replace("&", "§"));

                            event.setCancelled(true);

                            return;

                        }
                    }

                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotConfig.ADMINCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%message%", event.getMessage())
                                        .replace("%displayname%", user_prefix + event.getPlayer().getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%server%", "")
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("&", "§")));

                    } else {

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotConfig.ADMINCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%message%", event.getMessage())
                                        .replace("%server%", "")
                                        .replace("&", "§")));

                    }

                    if (SpigotConfig.DISCORD_ENABLED.get(Boolean.class) && SpigotConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotConfig.ADMIN_CHANNEL_ID.get(String.class));

                        assert channel != null;
                        channel.sendMessageFormat(SpigotConfig.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%message%", message)
                                        .replace("%server%", ""))
                                .queue();

                    }

                    event.setCancelled(true);

                } else {

                    PlayerCache.getToggled_2_admin().remove(event.getPlayer().getUniqueId());

                }
            }
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {

            return;

        }

        if (!event.getChannel().getId().equalsIgnoreCase(SpigotConfig.ADMIN_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(SpigotConfig.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            new BukkitRunnable() {
                @Override
                public void run() {

                    event.getMessage().delete().queue();
                    cancel();

                }

            }.runTaskTimer(PLUGIN, Math.multiplyExact(5, 20), 1);

            return;

        }

        if (event.getAuthor().isBot()) {
            return;
        }

        if (PlayerCache.getMuted_admin().contains("true")) {

            event.getMessage().reply(SpigotConfig.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            new BukkitRunnable() {
                @Override
                public void run() {

                    event.getMessage().delete().queue();
                    cancel();

                }

            }.runTaskTimer(PLUGIN, Math.multiplyExact(5, 20), 1);

            return;

        }

        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                        (players -> players.hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                .forEach(players -> players.sendMessage(SpigotConfig.DISCORD_ADMIN_FORMAT.color()
                        .replace("%prefix%", SpigotConfig.ADMINPREFIX.color())
                        .replace("%user%", event.getAuthor().getName())
                        .replace("%message%", event.getMessage().getContentDisplay())));

    }
}