package it.frafol.cleanstaffchat.bungee.adminchat.listeners;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.*;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(ChatEvent event) {

        if (BungeeConfig.ADMINCHAT_PREFIX_MODULE.get(Boolean.class) && event.getMessage().startsWith(BungeeConfig.ADMINCHAT_PREFIX.get(String.class))) {
            final String message = event.getMessage().substring(1);
            if (!((ProxiedPlayer) event.getSender()).hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) return;
            if (BungeeServers.ADMINCHAT_ENABLE.get(Boolean.class)) {
                for (String server : BungeeServers.AC_BLOCKED_SRV.getStringList()) {
                    if (((ProxiedPlayer) event.getSender()).getServer() == null) return;
                    if (((ProxiedPlayer) event.getSender()).getServer().getInfo().getName().equalsIgnoreCase(server)) {
                        event.setCancelled(true);
                        PlayerCache.sendChannelMessage((ProxiedPlayer) event.getSender(), false);
                        ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_MUTED_ERROR.color()
                                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
                        return;
                    }
                }
            }
            if (!BungeeConfig.WORKAROUND_KICK.get(Boolean.class)) {
                event.setCancelled(true);
            }

            if (BungeeConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
                if (PlayerCache.hasColorCodes(message)) {

                    ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacy(BungeeMessages.COLOR_CODES.color()
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                            .replace("&", "§")));

                    return;

                }
            }

            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                LuckPerms api = LuckPermsProvider.get();

                User user = api.getUserManager().getUser(((ProxiedPlayer) event.getSender()).getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.ADMINCHAT_FORMAT.get(String.class)
                            .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                            .replace("%message%", message)
                            .replace("%displayname%", ((ProxiedPlayer) event.getSender()).getName())
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedAdminChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%displayname%", PlayerCache.translateHex(user_prefix) + ((ProxiedPlayer) event.getSender()).getName() + PlayerCache.translateHex(user_suffix))
                                .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                .replace("&", "§"))));

            } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(((ProxiedPlayer) event.getSender()).getUniqueId()).isPresent()) {
                    return;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((ProxiedPlayer) event.getSender()).getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.ADMINCHAT_FORMAT.get(String.class)
                            .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                            .replace("%message%", message)
                            .replace("%displayname%", ultraPermissionsUserPrefixFinal + ((ProxiedPlayer) event.getSender()).getName() + ultraPermissionsUserSuffixFinal)
                            .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                            .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                            .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedAdminChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + ((ProxiedPlayer) event.getSender()).getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                .replace("&", "§"))));

            } else {

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedAdminChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                .replace("&", "§"))));
            }

            if (!BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !BungeeConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return;
            }

            final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeDiscordConfig.ADMIN_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(BungeeDiscordConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(BungeeMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                        .replace("%message%", message)
                        .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName()));

                embed.setColor(Color.getColor(BungeeDiscordConfig.EMBEDS_ADMINCHATCOLOR.get(String.class)));
                embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {

                channel.sendMessageFormat(BungeeMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName()))
                        .queue();

            }
        }

        final String message = event.getMessage();
        if (!PlayerCache.getToggled_2_admin().contains(((ProxiedPlayer) event.getSender()).getUniqueId())) {
            return;
        }

        if (PlayerCache.getMuted_admin().contains("true")) {
            PlayerCache.getToggled_2_admin().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
            event.setCancelled(true);
            PlayerCache.sendChannelMessage((ProxiedPlayer) event.getSender(), false);
            ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_MUTED_ERROR.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        if (event.getMessage().startsWith("/")) {
            return;
        }

        if (BungeeServers.ADMINCHAT_ENABLE.get(Boolean.class)) {
            for (String server : BungeeServers.AC_BLOCKED_SRV.getStringList()) {

                if (((ProxiedPlayer) event.getSender()).getServer() == null) {
                    return;
                }

                if (((ProxiedPlayer) event.getSender()).getServer().getInfo().getName().equalsIgnoreCase(server)) {
                    PlayerCache.getToggled_2_admin().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
                    event.setCancelled(true);
                    PlayerCache.sendChannelMessage((ProxiedPlayer) event.getSender(), false);
                    ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_MUTED_ERROR.color()
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
                    return;
                }
            }
        }

        if (!(BungeeConfig.ADMINCHAT_TALK_MODULE.get(Boolean.class))) {
            ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacy(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                    .replace("&", "§")));

        } else if (((ProxiedPlayer) event.getSender()).hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) {

            if (!BungeeConfig.WORKAROUND_KICK.get(Boolean.class)) {
                event.setCancelled(true);
            }

            if (BungeeConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
                if (PlayerCache.hasColorCodes(message)) {

                    ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacy(BungeeMessages.COLOR_CODES.color()
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                            .replace("&", "§")));

                    return;

                }
            }

            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                LuckPerms api = LuckPermsProvider.get();

                User user = api.getUserManager().getUser(((ProxiedPlayer) event.getSender()).getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.ADMINCHAT_FORMAT.get(String.class)
                            .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                            .replace("%message%", message)
                            .replace("%displayname%", ((ProxiedPlayer) event.getSender()).getName())
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedAdminChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%displayname%", PlayerCache.translateHex(user_prefix) + ((ProxiedPlayer) event.getSender()).getName() + PlayerCache.translateHex(user_suffix))
                                .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                .replace("&", "§"))));

            } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(((ProxiedPlayer) event.getSender()).getUniqueId()).isPresent()) {
                    return;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((ProxiedPlayer) event.getSender()).getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.ADMINCHAT_FORMAT.get(String.class)
                            .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                            .replace("%message%", message)
                            .replace("%displayname%", ultraPermissionsUserPrefixFinal + ((ProxiedPlayer) event.getSender()).getName() + ultraPermissionsUserSuffixFinal)
                            .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                            .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                            .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedAdminChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + ((ProxiedPlayer) event.getSender()).getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                .replace("&", "§"))));

            } else {

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedAdminChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                .replace("&", "§"))));
            }

            if (!BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !BungeeConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return;
            }

            final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeDiscordConfig.ADMIN_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(BungeeDiscordConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(BungeeMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                        .replace("%message%", message)
                        .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName()));

                embed.setColor(Color.getColor(BungeeDiscordConfig.EMBEDS_ADMINCHATCOLOR.get(String.class)));
                embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {

                channel.sendMessageFormat(BungeeMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                .replace("%message%", message)
                                .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName()))
                        .queue();

            }
        } else {
            PlayerCache.getToggled_2_admin().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {
            return;
        }

        if (!event.getChannel().getId().equalsIgnoreCase(BungeeDiscordConfig.ADMIN_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(BungeeMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {
            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);
            return;
        }

        if (event.getAuthor().isBot() && !BungeeDiscordConfig.FORWARD_BOT.get(Boolean.class)) {
            return;
        }

        if (PlayerCache.getMuted_admin().contains("true")) {
            event.getMessage().reply(BungeeMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();
            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);
            return;
        }

        if (PLUGIN.getProxy().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
            final String final_message = BungeeMessages.DISCORD_ADMIN_FORMAT.get(String.class)
                    .replace("%user%", event.getAuthor().getName())
                    .replace("%message%", event.getMessage().getContentDisplay())
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                    .replace("&", "§");

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);

        } else {

            CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                            (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                    && !CleanStaffChat.getInstance().isInBlockedAdminChatServer(players))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.DISCORD_ADMIN_FORMAT.color()
                            .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())
                            .replace("%user%", event.getAuthor().getName())
                            .replace("%message%", event.getMessage().getContentDisplay()))));
        }
    }
}