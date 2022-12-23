package it.frafol.cleanstaffchat.bungee.staffchat.listeners;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.UpdateCheck;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.enums.BungeeRedis;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.awt.*;

public class JoinListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void handle(PostLoginEvent event){

        if (event.getPlayer().hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                && (BungeeConfig.UPDATE_CHECK.get(Boolean.class)) && !PLUGIN.getDescription().getVersion().contains("alpha")) {
            new UpdateCheck(PLUGIN).getVersion(version -> {
                if (!PLUGIN.getDescription().getVersion().equals(version)) {
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText("[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz"));
                    PLUGIN.getLogger().warning("§eThere is a new update available, download it on SpigotMC!");
                }
            });
        }

        if (!(CleanStaffChat.getInstance().getProxy().getPlayers().size() < 1)) {

            final ProxiedPlayer player = event.getPlayer();

            if (BungeeConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {

                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || BungeeConfig.STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {

                    if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                        assert user != null;

                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();

                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final String final_message = BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName())
                                    .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                    .replace("%userprefix%", user_prefix)
                                    .replace("%usersuffix%", user_suffix)
                                    .replace("%prefix%", BungeeMessages.PREFIX.color())
                                    .replace("%server%", player.getServer().getInfo().getName())
                                    .replace("&", "§");


                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                            return;

                        }

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
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
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFF_JOIN_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%server%", player.getServer().getInfo().getName())
                                        .replace("%displayname%", player.getName())
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%user%", player.getName()))));

                    }

                    if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                            && BungeeConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                            && BungeeConfig.JOIN_LEAVE_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                        assert channel != null;

                        if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(BungeeDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(BungeeMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName()));

                            embed.setColor(Color.YELLOW);
                            embed.setFooter("Powered by CleanStaffChat");

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
    public void handle(PlayerDisconnectEvent event) {

        final ProxiedPlayer player = event.getPlayer();

        PlayerCache.getAfk().remove(player.getUniqueId());

        if (BungeeConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {

            if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                    || BungeeConfig.STAFFCHAT_QUIT_ALL.get(Boolean.class)) {

                if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                    LuckPerms api = LuckPermsProvider.get();

                    User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                    assert user != null;
                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();
                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                        final String final_message = BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getName())
                                .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                .replace("%userprefix%", user_prefix)
                                .replace("%usersuffix%", user_suffix)
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%server%", player.getServer().getInfo().getName())
                                .replace("&", "§");


                        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                        redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                        return;

                    }

                    if (CleanStaffChat.getInstance().getProxy().getPlayers().size() >= 1) {

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeMessages.PREFIX.color())
                                        .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
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

                    if (CleanStaffChat.getInstance().getProxy().getPlayers().size() >= 1) {

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFF_QUIT_MESSAGE_FORMAT.color()
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
                            && BungeeConfig.JOIN_LEAVE_DISCORD_MODULE.get(Boolean.class)) {

                    final TextChannel channel = PLUGIN.getJda().getTextChannelById(BungeeDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                    assert channel != null;

                    if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                        EmbedBuilder embed = new EmbedBuilder();

                        embed.setTitle(BungeeDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                        embed.setDescription(BungeeMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getName()));

                        embed.setColor(Color.YELLOW);
                        embed.setFooter("Powered by CleanStaffChat");

                        channel.sendMessageEmbeds(embed.build()).queue();

                    } else {

                        channel.sendMessageFormat(BungeeMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getName())).queue();

                    }
                }
            }
        }

        PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

    }
}
