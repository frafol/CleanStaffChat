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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

public class JoinListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void handle(@NotNull PlayerJoinEvent event) {

        if (event.getPlayer().hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                && (SpigotConfig.UPDATE_CHECK.get(Boolean.class)) && !PLUGIN.getDescription().getVersion().contains("alpha")) {
            PLUGIN.UpdateCheck(event.getPlayer());
        }

        if (CleanStaffChat.getInstance().getServer().getOnlinePlayers().isEmpty()) {
            return;
        }

        final Player player = event.getPlayer();

        if (!SpigotConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {
            return;
        }

        if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                || SpigotConfig.STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {

            if (player.hasPermission(SpigotConfig.STAFFCHAT_JOIN_SILENT_PERMISSION.get(String.class)) && SpigotConfig.STAFFCHAT_JOIN_SILENT_MODULE.get(Boolean.class)) {
                return;
            }

            if (PLUGIN.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                LuckPerms api = LuckPermsProvider.get();

                User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();

                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission
                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFF_JOIN_MESSAGE_FORMAT.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%displayname%", PlayerCache.color(user_prefix) + player.getName() + PlayerCache.color(user_suffix))
                                .replace("%userprefix%", PlayerCache.color(user_prefix))
                                .replace("%usersuffix%", PlayerCache.color(user_suffix))
                                .replace("%user%", player.getName())));

            } else if (PLUGIN.getServer().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(player.getUniqueId()).isPresent()) {
                    return;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(player.getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission
                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFF_JOIN_MESSAGE_FORMAT.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%user%", player.getName())));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission
                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFF_JOIN_MESSAGE_FORMAT.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())));

            }

            if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE.get(Boolean.class)) {

                final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return;
                }

                if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(SpigotMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName()));

                    embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                    embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {
                    channel.sendMessageFormat(SpigotMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())).queue();
                }
            }
        }
    }

    @EventHandler
    public void handle(@NotNull PlayerQuitEvent event) {

        final Player player = event.getPlayer();

        PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

        if (!SpigotConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {
            return;
        }

        if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                || SpigotConfig.STAFFCHAT_QUIT_ALL.get(Boolean.class)) {

            if (player.hasPermission(SpigotConfig.STAFFCHAT_QUIT_SILENT_PERMISSION.get(String.class)) && SpigotConfig.STAFFCHAT_QUIT_SILENT_MODULE.get(Boolean.class)) {
                return;
            }

            if (PLUGIN.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                LuckPerms api = LuckPermsProvider.get();

                User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();

                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission
                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%displayname%", PlayerCache.color(user_prefix) + player.getName() + PlayerCache.color(user_suffix))
                                .replace("%userprefix%", PlayerCache.color(user_prefix))
                                .replace("%usersuffix%", PlayerCache.color(user_suffix))
                                .replace("%user%", player.getName())));

            } else if (PLUGIN.getServer().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(player.getUniqueId()).isPresent()) {
                    return;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(player.getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission
                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%user%", player.getName())));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission
                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%displayname%", player.getName())
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%user%", player.getName())));

            }

            if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE.get(Boolean.class)) {

                final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return;
                }

                if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(SpigotMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName()));

                    embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                    embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {
                    channel.sendMessageFormat(SpigotMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())).queue();
                }
            }
        }
    }
}
