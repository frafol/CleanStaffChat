package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
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

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.instance;

public class ServerListener {

    public final CleanStaffChat PLUGIN;

    public ServerListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void Switch(@NotNull ServerConnectedEvent event) {

        if (!event.getPreviousServer().isPresent()) {

            if (event.getPlayer().hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                    && (VelocityConfig.UPDATE_CHECK.get(Boolean.class))) {
                PLUGIN.UpdateCheck(event.getPlayer());
            }
            return;
        }

        if (VelocityConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)
                && PlayerCache.getAfk().contains(event.getPlayer().getUniqueId())) {

            if (event.getPlayer().hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

                if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                    if (user == null) {
                        return;
                    }

                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();
                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                        final String final_message = VelocityMessages.STAFFCHAT_AFK_OFF.get(String.class)
                                .replace("%user%", event.getPlayer().getUsername())
                                .replace("%displayname%", ChatUtil.translateHex(user_prefix) + event.getPlayer().getUsername() + ChatUtil.translateHex(user_suffix))
                                .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                                .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                                .replace("%prefix%", VelocityMessages.PREFIX.color())
                                .replace("%server%", event.getServer().getServerInfo().getName())
                                .replace("&", "§");


                        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
                        redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffAFKMessage-RedisBungee", final_message);
                        return;
                    }

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                            && !instance.isInBlockedStaffChatServer(players))
                            .forEach(players -> VelocityMessages.STAFFCHAT_AFK_OFF.send(players,
                                    new Placeholder("user", event.getPlayer().getUsername()),
                                    new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + event.getPlayer() + ChatUtil.translateHex(user_suffix)),
                                    new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                                    new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                                    new Placeholder("server", event.getServer().getServerInfo().getName()),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                } else {

                    if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                        final String final_message = VelocityMessages.STAFFCHAT_AFK_OFF.get(String.class)
                                .replace("%user%", event.getPlayer().getUsername())
                                .replace("%displayname%", event.getPlayer().getUsername())
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%prefix%", VelocityMessages.PREFIX.color())
                                .replace("%server%", event.getServer().getServerInfo().getName())
                                .replace("&", "§");


                        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
                        redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffAFKMessage-RedisBungee", final_message);
                        return;
                    }

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                            && !instance.isInBlockedStaffChatServer(players))
                            .forEach(players -> VelocityMessages.STAFFCHAT_AFK_OFF.send(players,
                                    new Placeholder("user", event.getPlayer().getUsername()),
                                    new Placeholder("displayname", event.getPlayer().getUsername()),
                                    new Placeholder("userprefix", ""),
                                    new Placeholder("usersuffix", ""),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));
                }

                PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());
                if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                        && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                        && VelocityConfig.STAFFCHAT_DISCORD_AFK_MODULE.get(Boolean.class)) {

                    final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                    if (channel == null) {
                        return;
                    }

                    if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

                        EmbedBuilder embed = new EmbedBuilder();

                        embed.setTitle(VelocityDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                        embed.setDescription(VelocityMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", event.getPlayer().getUsername()));

                        embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                        embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                        channel.sendMessageEmbeds(embed.build()).queue();

                    } else {

                        channel.sendMessageFormat(VelocityMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT.get(String.class)
                                        .replace("%user%", event.getPlayer().getUsername()))
                                .queue();

                    }
                }
            }
        }

        if (!(CleanStaffChat.getInstance().getServer().getAllPlayers().isEmpty())) {

            final Player player = event.getPlayer();

            if (VelocityConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) {

                if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || VelocityConfig.STAFFCHAT_SWITCH_ALL.get(Boolean.class)) {

                    if (VelocityConfig.STAFFCHAT_SWITCH_SILENT_MODULE.get(Boolean.class) && player.hasPermission(VelocityConfig.STAFFCHAT_SWITCH_SILENT_PERMISSION.get(String.class))) {
                        return;
                    }

                    if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                        if (user == null) {
                            return;
                        }

                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final String final_message = VelocityMessages.STAFF_SWITCH_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getUsername())
                                    .replace("%displayname%", ChatUtil.translateHex(user_prefix) + player.getUsername() + ChatUtil.translateHex(user_suffix))
                                    .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                                    .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                                    .replace("%serverbefore%", event.getPreviousServer().get().getServerInfo().getName())
                                    .replace("%server%", event.getServer().getServerInfo().getName())
                                    .replace("&", "§");


                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);

                            return;

                        }

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                                && !instance.isInBlockedStaffChatServer(players))
                                .forEach(players -> VelocityMessages.STAFF_SWITCH_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                                        new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                                        new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                                        new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + player.getUsername() + ChatUtil.translateHex(user_suffix)),
                                        new Placeholder("serverbefore", event.getPreviousServer().get().getServerInfo().getName()),
                                        new Placeholder("server", event.getServer().getServerInfo().getName())));

                    } else {

                        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                            final String final_message = VelocityMessages.STAFF_SWITCH_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getUsername())
                                    .replace("%displayname%", player.getUsername())
                                    .replace("%userprefix%", "")
                                    .replace("%usersuffix%", "")
                                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                                    .replace("%serverbefore%", event.getPreviousServer().get().getServerInfo().getName())
                                    .replace("%server%", event.getServer().getServerInfo().getName())
                                    .replace("&", "§");


                            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
                            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffOtherMessage-RedisBungee", final_message);
                            return;
                        }

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                                && !instance.isInBlockedStaffChatServer(players))
                                .forEach(players -> VelocityMessages.STAFF_SWITCH_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                                        new Placeholder("userprefix", ""),
                                        new Placeholder("usersuffix", ""),
                                        new Placeholder("displayname", player.getUsername()),
                                        new Placeholder("serverbefore", event.getPreviousServer().get().getServerInfo().getName()),
                                        new Placeholder("server", event.getServer().getServerInfo().getName())));

                    }

                    if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                            && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                            && VelocityConfig.STAFFCHAT_DISCORD_SWITCH_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                        if (channel == null) {
                            return;
                        }

                        if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(VelocityDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(VelocityMessages.STAFF_DISCORD_SWITCH_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getUsername())
                                    .replace("%from%", event.getPreviousServer().get().getServerInfo().getName())
                                    .replace("%server%", event.getServer().getServerInfo().getName()));

                            embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                            embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {
                            channel.sendMessageFormat(VelocityMessages.STAFF_DISCORD_SWITCH_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getUsername())
                                    .replace("%from%", event.getPreviousServer().get().getServerInfo().getName())
                                    .replace("%server%", event.getServer().getServerInfo().getName())).queue();
                        }
                    }
                }
            }
        }
    }
}