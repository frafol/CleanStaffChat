package it.frafol.cleanstaffchat.bungee.staffchat.listeners;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.enums.BungeeRedis;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
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
import java.util.concurrent.TimeUnit;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(ChatEvent event) {

        String message = event.getMessage();

        if (PlayerCache.getToggled_2().contains(((ProxiedPlayer) event.getSender()).getUniqueId())) {
            if (PlayerCache.getMuted().contains("true")) {
                PlayerCache.getToggled_2().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
                event.setCancelled(true);
                ((ProxiedPlayer)event.getSender()).sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())));
                return;
            }

            if (!event.getMessage().startsWith("/")) {
                if (!(BungeeConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                    ((ProxiedPlayer)event.getSender()).sendMessage(TextComponent.fromLegacyText(BungeeMessages.MODULE_DISABLED.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("&", "§")));

                } else if (((ProxiedPlayer) event.getSender()).hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

                    event.setCancelled(true);

                    if (BungeeConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
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

                            ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacyText(BungeeMessages.COLOR_CODES.color()
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("&", "§")));

                            return;

                        }
                    }

                    if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(((ProxiedPlayer) event.getSender()).getUniqueId());
                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                            final String final_message = BungeeMessages.STAFFCHAT_FORMAT.get(String.class)
                                    .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                    .replace("%message%", message)
                                    .replace("%displayname%", user_prefix + ((ProxiedPlayer) event.getSender()).getName() + user_suffix)
                                    .replace("%userprefix%", user_prefix)
                                    .replace("%usersuffix%", user_suffix)
                                    .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("&", "§");

                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffMessage-RedisBungee", final_message);

                            return;

                        }

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                        .replace("%message%", message)
                                        .replace("%displayname%", user_prefix + ((ProxiedPlayer) event.getSender()).getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                        .replace("&", "§"))));

                    } else {

                        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                            final String final_message = BungeeMessages.STAFFCHAT_FORMAT.get(String.class)
                                    .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                    .replace("%message%", message)
                                    .replace("%displayname%", ((ProxiedPlayer) event.getSender()).getName())
                                    .replace("%userprefix%", "")
                                    .replace("%usersuffix%", "")
                                    .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("&", "§");

                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffMessage-RedisBungee", final_message);

                            return;

                        }

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                        .replace("%message%", message)
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                        .replace("&", "§"))));
                    }

                    if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && BungeeConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                        assert channel != null;

                        if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(BungeeDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(BungeeMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                    .replace("%message%", message)
                                    .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName()));

                            embed.setColor(Color.RED);
                            embed.setFooter("Powered by CleanStaffChat");

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {

                            channel.sendMessageFormat(BungeeMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                            .replace("%message%", message)
                                            .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName()))
                                    .queue();

                        }
                    }

                } else {

                    PlayerCache.getToggled_2().remove(((ProxiedPlayer) event.getSender()).getUniqueId());

                }
            }
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {

            return;

        }

        if (!event.getChannel().getId().equalsIgnoreCase(BungeeDiscordConfig.STAFF_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(BungeeMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);

            return;

        }

        if (event.getAuthor().isBot()) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {

            event.getMessage().reply(BungeeMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);

            return;

        }

        if (PLUGIN.getProxy().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

            final String final_message = BungeeMessages.DISCORD_STAFF_FORMAT.get(String.class)
                    .replace("%user%", event.getAuthor().getName())
                    .replace("%message%", event.getMessage().getContentDisplay())
                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                    .replace("&", "§");

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffMessage-RedisBungee", final_message);

        } else {
            CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                            (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.DISCORD_STAFF_FORMAT.color()
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("%user%", event.getAuthor().getName())
                            .replace("%message%", event.getMessage().getContentDisplay()))));
        }

    }
}