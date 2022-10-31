package it.frafol.cleanstaffchat.bungee.donorchat.listeners;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
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

import java.util.concurrent.TimeUnit;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(ChatEvent event) {

        String message = event.getMessage();

        if (PlayerCache.getToggled_2_donor().contains(((ProxiedPlayer) event.getSender()).getUniqueId())) {

            if (PlayerCache.getMuted_donor().contains("true")) {
                PlayerCache.getToggled_2_donor().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
                event.setCancelled(true);
                ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                return;
            }

            if (PlayerCache.getCooldown().contains(((ProxiedPlayer) event.getSender()).getUniqueId())) {
                PlayerCache.getToggled_2_donor().remove(((ProxiedPlayer) event.getSender()).getUniqueId());
                event.setCancelled(true);
                ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_COOLDOWN_MESSAGE.color()
                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                return;
            }

            if (!event.getMessage().startsWith("/")) {
                if (!(BungeeConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {
                    ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacyText(BungeeConfig.MODULE_DISABLED.color()
                            .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                            .replace("&", "§")));

                } else if (((ProxiedPlayer) event.getSender()).hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {

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

                            ((ProxiedPlayer) event.getSender()).sendMessage(TextComponent.fromLegacyText(BungeeConfig.COLOR_CODES.color()
                                    .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                                    .replace("&", "§")));

                            return;

                        }

                    }

                    if (!((ProxiedPlayer) event.getSender()).hasPermission(BungeeConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) {

                        PlayerCache.getCooldown().add(((ProxiedPlayer) event.getSender()).getUniqueId());

                        ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                                PlayerCache.getCooldown().remove(((ProxiedPlayer) event.getSender()).getUniqueId()), BungeeConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);

                    }

                    if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(((ProxiedPlayer) event.getSender()).getUniqueId());
                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                                        .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                        .replace("%message%", message)
                                        .replace("%displayname%", user_prefix + ((ProxiedPlayer) event.getSender()).getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                        .replace("&", "§"))));

                    } else {

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                                        .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                        .replace("%message%", message)
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName())
                                        .replace("&", "§"))));
                    }

                    if (BungeeConfig.DISCORD_ENABLED.get(Boolean.class) && BungeeConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeConfig.DONOR_CHANNEL_ID.get(String.class));

                        assert channel != null;
                        channel.sendMessageFormat(BungeeConfig.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                        .replace("%user%", ((ProxiedPlayer) event.getSender()).getName())
                                        .replace("%message%", message)
                                        .replace("%server%", ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName()))
                                .queue();

                    }

                } else {

                    PlayerCache.getToggled_2_donor().remove(((ProxiedPlayer) event.getSender()).getUniqueId());

                }

            }
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {

            return;

        }

        if (!event.getChannel().getId().equalsIgnoreCase(BungeeConfig.DONOR_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(BungeeConfig.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class))
                || event.getMessage().getContentDisplay().equalsIgnoreCase(BungeeConfig.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);

            return;

        }

        if (event.getAuthor().isBot()) {

            return;

        }

        if (PlayerCache.getMuted_donor().contains("true")) {

            event.getMessage().reply(BungeeConfig.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);

            return;

        }

        if (PlayerCache.getCooldown_discord().contains(event.getAuthor().getId())
                && (!BungeeConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class))) {

            event.getMessage().reply(BungeeConfig.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class)).queue();

            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);

            return;

        }

        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                        (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DISCORD_DONOR_FORMAT.color()
                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                        .replace("%user%", event.getAuthor().getName())
                        .replace("%message%", event.getMessage().getContentDisplay())
                        .replace("&", "§"))));

        if (!BungeeConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class)) {

            PlayerCache.getCooldown_discord().add(event.getAuthor().getId());

            ProxyServer.getInstance().getScheduler().schedule(PLUGIN, () ->
                    PlayerCache.getCooldown_discord().remove(event.getAuthor().getId()), BungeeConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);

        }
    }
}