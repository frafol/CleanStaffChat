package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

import java.awt.*;
import java.util.Optional;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        if (PlayerCache.getToggled_2().contains(event.getPlayer().getUniqueId())) {

            if (PlayerCache.getMuted().contains("true")) {

                PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());

                event.setCancelled(true);

                event.getPlayer().sendMessage(SpigotMessages.STAFFCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color()));

                return;

            }

            if (!event.getMessage().startsWith("/")) {

                if (!(SpigotConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {

                    event.getPlayer().sendMessage((SpigotMessages.MODULE_DISABLED.color()
                            .replace("%prefix%", SpigotMessages.PREFIX.color())
                            .replace("&", "§")));

                } else if (event.getPlayer().hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

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

                            event.getPlayer().sendMessage(SpigotMessages.COLOR_CODES.color()
                                    .replace("%prefix%", SpigotMessages.PREFIX.color())
                                    .replace("&", "§"));

                            event.setCancelled(true);

                            return;

                        }
                    }

                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

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
                                .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotMessages.PREFIX.color())
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%message%", event.getMessage())
                                        .replace("%displayname%", user_prefix + event.getPlayer().getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%server%", "")
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("&", "§")));

                    } else if (Bukkit.getServer().getPluginManager().getPlugin("UltraPermissions") != null) {

                        final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();
                        final UserList userList = ultraPermissionsAPI.getUsers();

                        if (!userList.uuid(event.getPlayer().getUniqueId()).isPresent()) {
                            return;
                        }

                        final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(event.getPlayer().getUniqueId()).get();

                        final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                        final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                        final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                        final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotMessages.PREFIX.color())
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%displayname%", ultraPermissionsUserPrefixFinal + event.getPlayer().getName() + ultraPermissionsUserSuffixFinal)
                                        .replace("%message%", message)
                                        .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                        .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                        .replace("%server%", "")
                                        .replace("&", "§")));

                    } else {

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotMessages.PREFIX.color())
                                        .replace("%user%", event.getPlayer().getName())
                                        .replace("%message%", event.getMessage())
                                        .replace("%server%", "")
                                        .replace("&", "§")));

                    }

                    if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                        if (channel == null) {return;}

                        if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", event.getPlayer().getName())
                                    .replace("%message%", message)
                                    .replace("%server%", ""));

                            embed.setColor(Color.RED);
                            embed.setFooter("Powered by CleanStaffChat");

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {

                            channel.sendMessageFormat(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", event.getPlayer().getName())
                                            .replace("%message%", message)
                                            .replace("%server%", ""))
                                    .queue();

                        }
                    }

                    event.setCancelled(true);

                } else {

                    PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());

                }
            }
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {

            return;

        }

        if (!event.getChannel().getId().equalsIgnoreCase(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

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

        if (PlayerCache.getMuted().contains("true")) {

            event.getMessage().reply(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

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
                .forEach(players -> players.sendMessage(SpigotMessages.DISCORD_STAFF_FORMAT.color()
                        .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())
                        .replace("%user%", event.getAuthor().getName())
                        .replace("%message%", event.getMessage().getContentDisplay())));

    }
}