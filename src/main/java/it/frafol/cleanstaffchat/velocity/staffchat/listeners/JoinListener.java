package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityDiscordConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.enums.VelocityRedis;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class JoinListener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    @SuppressWarnings("UnstableApiUsage")
    public void handle(@NotNull ServerPostConnectEvent event) {

        if (event.getPreviousServer() != null) {
            return;
        }

        final Player player = event.getPlayer();

        if (player.hasPermission(STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            PLUGIN.UpdateCheck(player);
        }

        if (!(CleanStaffChat.getInstance().getServer().getAllPlayers().isEmpty())) {

            if (!STAFF_JOIN_MESSAGE.get(Boolean.class)) {
                return;
            }

            if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                    || STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {

                if (!player.getCurrentServer().isPresent()) {
                    return;
                }

                PlayerCache.getStaffers().add(player.getUniqueId());

                if (player.hasPermission(STAFFCHAT_JOIN_SILENT_PERMISSION.get(String.class)) && STAFFCHAT_JOIN_SILENT_MODULE.get(Boolean.class)) {
                    return;
                }

                if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User user = api.getUserManager().getUser(player.getUniqueId());

                    if (user == null) {
                        return;
                    }

                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();
                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                        final String final_message = VelocityMessages.STAFF_JOIN_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getUsername())
                                .replace("%displayname%", ChatUtil.translateHex(user_prefix) + player.getUsername() + ChatUtil.translateHex(user_suffix))
                                .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                                .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                                .replace("%prefix%", VelocityMessages.PREFIX.color())
                                .replace("%server%", player.getCurrentServer().get().getServerInfo().getName())
                                .replace("&", "§");


                        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                        redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                        return;

                    }

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                            && !instance.isInBlockedStaffChatServer(players))
                            .forEach(players -> VelocityMessages.STAFF_JOIN_MESSAGE_FORMAT.send(players,
                                    new Placeholder("user", player.getUsername()),
                                    new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + player.getUsername() + ChatUtil.translateHex(user_suffix)),
                                    new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                                    new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                                    new Placeholder("server", player.getCurrentServer().get().getServerInfo().getName()),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                } else {

                    if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                        final String final_message = VelocityMessages.STAFF_JOIN_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getUsername())
                                .replace("%displayname%", player.getUsername())
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%prefix%", VelocityMessages.PREFIX.color())
                                .replace("&", "§");


                        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                        redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                        return;

                    }

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                            && !instance.isInBlockedStaffChatServer(players))
                            .forEach(players -> VelocityMessages.STAFF_JOIN_MESSAGE_FORMAT.send(players,
                                    new Placeholder("user", player.getUsername()),
                                    new Placeholder("displayname", player.getUsername()),
                                    new Placeholder("userprefix", ""),
                                    new Placeholder("usersuffix", ""),
                                    new Placeholder("server", player.getCurrentServer().get().getServerInfo().getName()),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                }

                if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class) && VelocityConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE.get(Boolean.class)) {

                    final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                    if (channel == null) {
                        return;
                    }

                    if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

                        EmbedBuilder embed = new EmbedBuilder();

                        embed.setTitle(VelocityDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                        embed.setDescription(VelocityMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getUsername()));

                        embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                        embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                        channel.sendMessageEmbeds(embed.build()).queue();

                    } else {

                        channel.sendMessageFormat(VelocityMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                        .replace("%user%", player.getUsername()))
                                .queue();

                    }
                }
            }
        }
    }

    @Subscribe
    public void handle(@NotNull DisconnectEvent event) {

        final Player player = event.getPlayer();

        PlayerCache.getAfk().remove(player.getUniqueId());
        PlayerCache.getStaffers().remove(player.getUniqueId());

        if (!player.getCurrentServer().isPresent()) {
            return;
        }

        if (!STAFF_QUIT_MESSAGE.get(Boolean.class)) {
            return;
        }

        if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                || STAFFCHAT_QUIT_ALL.get(Boolean.class)) {

            if (player.hasPermission(STAFFCHAT_QUIT_SILENT_PERMISSION.get(String.class)) && STAFFCHAT_QUIT_SILENT_MODULE.get(Boolean.class)) {
                return;
            }

            if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                LuckPerms api = LuckPermsProvider.get();

                if (api.getUserManager().getUser(event.getPlayer().getUniqueId()) == null) {
                    return;
                }

                User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();

                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final String final_message = VelocityMessages.STAFF_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getUsername())
                            .replace("%displayname%", ChatUtil.translateHex(user_prefix) + player.getUsername() + ChatUtil.translateHex(user_suffix))
                            .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                            .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                            .replace("%server%", player.getCurrentServer().get().getServerInfo().getName())
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("&", "§");


                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                    return;

                }

                if (!CleanStaffChat.getInstance().getServer().getAllPlayers().isEmpty()) {

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                            && !instance.isInBlockedStaffChatServer(players))
                            .forEach(players -> VelocityMessages.STAFF_QUIT_MESSAGE_FORMAT.send(players,
                                    new Placeholder("user", player.getUsername()),
                                    new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + player.getUsername() + ChatUtil.translateHex(user_suffix)),
                                    new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                                    new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                                    new Placeholder("server", player.getCurrentServer().get().getServerInfo().getName()),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                }

            } else {

                if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final String final_message = VelocityMessages.STAFF_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getUsername())
                            .replace("%displayname%", player.getUsername())
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", player.getCurrentServer().get().getServerInfo().getName())
                            .replace("%prefix%", VelocityMessages.PREFIX.color())
                            .replace("&", "§");


                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                    return;

                }

                if (!CleanStaffChat.getInstance().getServer().getAllPlayers().isEmpty()) {

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                            && !instance.isInBlockedStaffChatServer(players))
                            .forEach(players -> VelocityMessages.STAFF_QUIT_MESSAGE_FORMAT.send(players,
                                    new Placeholder("user", player.getUsername()),
                                    new Placeholder("displayname", player.getUsername()),
                                    new Placeholder("userprefix", ""),
                                    new Placeholder("usersuffix", ""),
                                    new Placeholder("server", player.getCurrentServer().get().getServerInfo().getName()),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                }

            }

            if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                    && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                    && VelocityConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE.get(Boolean.class)) {

                final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return;
                }

                if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(VelocityDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(VelocityMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getUsername()));

                    embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                    embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {

                    channel.sendMessageFormat(VelocityMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getUsername()))
                            .queue();

                }
            }
        }
    }
}
