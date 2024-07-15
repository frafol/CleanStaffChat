package it.frafol.cleanstaffchat.bungee.staffchat.listeners;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.enums.BungeeRedis;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

public class JoinListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void handle(@NotNull ServerSwitchEvent event){

        final ProxiedPlayer player = event.getPlayer();

        if (event.getFrom() != null) {
            return;
        }

        if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                && (BungeeConfig.UPDATE_CHECK.get(Boolean.class)) && !PLUGIN.getDescription().getVersion().contains("alpha")) {
            PLUGIN.UpdateCheck(player);
        }

        if (!(CleanStaffChat.getInstance().getProxy().getPlayers().isEmpty())) {

            if (BungeeConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {

                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || BungeeConfig.STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {

                    if (player.hasPermission(BungeeConfig.STAFFCHAT_JOIN_SILENT_PERMISSION.get(String.class)) && BungeeConfig.STAFFCHAT_JOIN_SILENT_MODULE.get(Boolean.class)) {
                        return;
                    }

                    if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(player.getUniqueId());

                        if (user == null) {
                            return;
                        }

                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();

                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final String final_message = BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName())
                                    .replace("%displayname%", PlayerCache.translateHex(user_prefix) + player.getName() + PlayerCache.translateHex(user_suffix))
                                    .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                    .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                                    .replace("%server%", player.getServer().getInfo().getName())
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("&", "§");


                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                            return;

                        }

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())) 
                                                && !CleanStaffChat.getInstance().isInBlockedStaffChatServer(players))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%displayname%", PlayerCache.translateHex(user_prefix) + player.getName() + PlayerCache.translateHex(user_suffix))
                                        .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                        .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                                        .replace("%server%", player.getServer().getInfo().getName())
                                        .replace("%user%", player.getName()))));

                    } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {

                        final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
                        final UserList userList = ultraPermissionsAPI.getUsers();

                        if (!userList.uuid(player.getUniqueId()).isPresent()) {
                            return;
                        }

                        final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(player.getUniqueId()).get();

                        final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                        final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                        final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                        final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final String final_message = BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName())
                                    .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                                    .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                    .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("%server%", player.getServer().getInfo().getName())
                                    .replace("&", "§");


                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                            return;

                        }

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())) 
                                                && !CleanStaffChat.getInstance().isInBlockedStaffChatServer(players))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                                        .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                        .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                        .replace("%server%", player.getServer().getInfo().getName())
                                        .replace("%user%", player.getName()))));

                    } else {

                        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final String final_message = BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName())
                                    .replace("%displayname%", player.getName())
                                    .replace("%userprefix%", "")
                                    .replace("%usersuffix%", "")
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("%server%", player.getServer().getInfo().getName())
                                    .replace("&", "§");


                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                            return;

                        }

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())) 
                                                && !CleanStaffChat.getInstance().isInBlockedStaffChatServer(players))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%server%", player.getServer().getInfo().getName())
                                        .replace("%displayname%", player.getName())
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%server%", player.getServer().getInfo().getName())
                                        .replace("%user%", player.getName()))));

                    }

                    if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                            && BungeeConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                            && BungeeConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                        if (channel == null) {
                            return;
                        }

                        if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(BungeeDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(BungeeMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName()));

                            embed.setColor(Color.getColor(BungeeDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                            embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {

                            channel.sendMessageFormat(BungeeMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName())).queue();

                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void handle(@NotNull PlayerDisconnectEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        PlayerCache.getAfk().remove(player.getUniqueId());

        if (player.getServer() == null) {
            return;
        }

        if (!BungeeConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {
            PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());
            return;
        }

        if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                || BungeeConfig.STAFFCHAT_QUIT_ALL.get(Boolean.class)) {

            if (player.hasPermission(BungeeConfig.STAFFCHAT_QUIT_SILENT_PERMISSION.get(String.class)) && BungeeConfig.STAFFCHAT_QUIT_SILENT_MODULE.get(Boolean.class)) {
                return;
            }

            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                LuckPerms api = LuckPermsProvider.get();

                User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                if (user == null) {
                    return;
                }
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final String final_message = BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())
                            .replace("%displayname%", PlayerCache.translateHex(user_prefix) + player.getName() + PlayerCache.translateHex(user_suffix))
                            .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                            .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%server%", player.getServer().getInfo().getName())
                            .replace("&", "§");


                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                    return;

                }

                if (!CleanStaffChat.getInstance().getProxy().getPlayers().isEmpty()) {

                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                            && !CleanStaffChat.getInstance().isInBlockedStaffChatServer(players))
                            .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("%displayname%", PlayerCache.translateHex(user_prefix) + player.getName() + PlayerCache.translateHex(user_suffix))
                                    .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                    .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                                    .replace("%server%", player.getServer().getInfo().getName())
                                    .replace("%user%", player.getName()))));

                }

            } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(player.getUniqueId()).isPresent()) {
                    return;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(player.getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final String final_message = BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())
                            .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                            .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                            .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%server%", player.getServer().getInfo().getName())
                            .replace("&", "§");


                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                    return;

                }

                if (!CleanStaffChat.getInstance().getProxy().getPlayers().isEmpty()) {

                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())
                                            && !CleanStaffChat.getInstance().isInBlockedStaffChatServer(players)))
                            .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                                    .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                    .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                    .replace("%server%", player.getServer().getInfo().getName())
                                    .replace("%user%", player.getName()))));

                }

            } else {

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final String final_message = BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())
                            .replace("%displayname%", player.getName())
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%server%", player.getServer().getInfo().getName())
                            .replace("&", "§");


                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);
                    return;
                }

                if (!CleanStaffChat.getInstance().getProxy().getPlayers().isEmpty()) {

                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())
                                            && !CleanStaffChat.getInstance().isInBlockedStaffChatServer(players)))
                            .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("%server%", player.getServer().getInfo().getName())
                                    .replace("%displayname%", player.getName())
                                    .replace("%userprefix%", "")
                                    .replace("%usersuffix%", "")
                                    .replace("%user%", player.getName()))));

                }
            }

            if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                    && BungeeConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                    && BungeeConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE.get(Boolean.class)) {

                final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return;
                }

                if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(BungeeDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(BungeeMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName()));

                    embed.setColor(Color.getColor(BungeeDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                    embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {
                    channel.sendMessageFormat(BungeeMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())).queue();
                }
            }
        }
    }
}
