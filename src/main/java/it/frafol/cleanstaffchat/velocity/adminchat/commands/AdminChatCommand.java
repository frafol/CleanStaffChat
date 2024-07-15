package it.frafol.cleanstaffchat.velocity.adminchat.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.*;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.awt.*;
import java.util.Arrays;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class AdminChatCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;
    public ProxyServer server;

    public AdminChatCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource commandSource = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {

            if (!(commandSource instanceof Player)) {
                VelocityMessages.ADMINARGUMENTS.send(commandSource, new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
                return;
            }

            if (commandSource.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) {

                Player player = (Player) commandSource;

                if (VelocityServers.ADMINCHAT_ENABLE.get(Boolean.class)) {
                    for (String server : VelocityServers.AC_BLOCKED_SRV.getStringList()) {

                        if (!player.getCurrentServer().isPresent()) {
                            return;
                        }

                        if (player.getCurrentServer().get().getServer().getServerInfo().getName().equalsIgnoreCase(server)) {
                            VelocityMessages.ADMINCHAT_MUTED_ERROR.send(player, new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
                            return;
                        }
                    }
                }

                if (!(ADMINCHAT_TALK_MODULE.get(Boolean.class))) {
                    VelocityMessages.ADMINARGUMENTS.send(commandSource, new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
                    return;
                }

                if (!PlayerCache.getToggled_2_admin().contains(player.getUniqueId())) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        PlayerCache.getToggled_2_admin().add(player.getUniqueId());
                        PlayerCache.getToggled_2_donor().remove(player.getUniqueId());
                        PlayerCache.getToggled_2().remove(player.getUniqueId());
                        ChatUtil.sendChannelMessage(player, true);
                        VelocityMessages.ADMINCHAT_TALK_ENABLED.send(commandSource,
                                new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));

                        return;
                    } else {
                        VelocityMessages.ADMINARGUMENTS.send(commandSource,
                                new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
                    }

                } else if (PlayerCache.getToggled_2_admin().contains(player.getUniqueId())) {

                    PlayerCache.getToggled_2_admin().remove(player.getUniqueId());
                    PlayerCache.getToggled_2().remove(player.getUniqueId());
                    PlayerCache.getToggled_2_donor().remove(player.getUniqueId());
                    ChatUtil.sendChannelMessage(player, false);
                    VelocityMessages.ADMINCHAT_TALK_DISABLED.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));

                    return;
                }

            } else {
                if (HIDE_ADVERTS.get(Boolean.class) != null && !HIDE_ADVERTS.get(Boolean.class)) {
                    commandSource.sendMessage(Component.text("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));
                }
                return;
            }
        }

        final String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        final String sender = !(commandSource instanceof Player) ? CONSOLE_PREFIX.get(String.class) :
                ((Player) commandSource).getUsername();

        if (!commandSource.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {
            VelocityMessages.ADMINCHAT_MUTED_ERROR.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            return;
        }

        if (commandSource instanceof Player) {

            if (PREVENT_COLOR_CODES.get(Boolean.class)) {
                if (ChatUtil.hasColorCodes(message)) {
                    VelocityMessages.COLOR_CODES.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
                    return;
                }
            }

            if (!((Player) commandSource).getCurrentServer().isPresent()) {
                return;
            }

            if (VelocityServers.ADMINCHAT_ENABLE.get(Boolean.class)) {
                for (String server : VelocityServers.AC_BLOCKED_SRV.getStringList()) {
                    if (((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName().equalsIgnoreCase(server)) {
                        VelocityMessages.ADMINCHAT_MUTED_ERROR.send(commandSource, new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
                        return;
                    }
                }
            }

            if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = VelocityMessages.ADMINCHAT_FORMAT.get(String.class)
                            .replace("%user%", sender)
                            .replace("%message%", message)
                            .replace("%displayname%", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix))
                            .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                            .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                            .replace("%server%", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName())
                            .replace("%prefix%", VelocityMessages.ADMINPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);
                    return;

                }

                CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !instance.isInBlockedAdminChatServer(players))
                        .forEach(players -> VelocityMessages.ADMINCHAT_FORMAT.send(players,
                                new Placeholder("user", sender),
                                new Placeholder("message", message),
                                new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix)),
                                new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                                new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                                new Placeholder("server", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()),
                                new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color())));

            } else {

                if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = VelocityMessages.ADMINCHAT_FORMAT.get(String.class)
                            .replace("%user%", sender)
                            .replace("%message%", message)
                            .replace("%displayname%", sender)
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName())
                            .replace("%prefix%", VelocityMessages.ADMINPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);
                    return;

                }

                CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                        && !instance.isInBlockedAdminChatServer(players))
                        .forEach(players -> VelocityMessages.ADMINCHAT_FORMAT.send(players,
                                new Placeholder("user", sender),
                                new Placeholder("message", message),
                                new Placeholder("displayname", sender),
                                new Placeholder("userprefix", ""),
                                new Placeholder("usersuffix", ""),
                                new Placeholder("server", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()),
                                new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color())));

            }

            if (!VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return;
            }

            final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.ADMIN_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(VelocityDiscordConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(VelocityMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", sender)
                        .replace("%message%", message)
                        .replace("%server%", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()));

                embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_ADMINCHATCOLOR.get(String.class)));
                embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {

                channel.sendMessageFormat(VelocityMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", sender)
                                .replace("%message%", message)
                                .replace("%server%", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()))
                        .queue();

            }

        } else if (CONSOLE_CAN_TALK.get(Boolean.class)) {

            if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                final String final_message = VelocityMessages.ADMINCHAT_CONSOLE_FORMAT.get(String.class)
                        .replace("%user%", sender)
                        .replace("%message%", message)
                        .replace("%displayname%", sender)
                        .replace("%userprefix%", "")
                        .replace("%usersuffix%", "")
                        .replace("%server%", "")
                        .replace("%prefix%", VelocityMessages.ADMINPREFIX.color())
                        .replace("&", "§");

                redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);

                return;

            }

            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                    && !instance.isInBlockedAdminChatServer(players))
                    .forEach(players -> VelocityMessages.ADMINCHAT_CONSOLE_FORMAT.send(players,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("displayname", sender),
                            new Placeholder("userprefix", ""),
                            new Placeholder("usersuffix", ""),
                            new Placeholder("server", ""),
                            new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color())));

            if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {

                final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.ADMIN_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return;
                }

                if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(VelocityDiscordConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(VelocityMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                            .replace("%user%", sender)
                            .replace("%message%", message)
                            .replace("%server%", ""));

                    embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_ADMINCHATCOLOR.get(String.class)));
                    embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {

                    channel.sendMessageFormat(VelocityMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", sender)
                                    .replace("%message%", message)
                                    .replace("%server%", ""))
                            .queue();

                }
            }

            VelocityMessages.ADMINCHAT_CONSOLE_FORMAT.send(commandSource,
                    new Placeholder("user", sender),
                    new Placeholder("message", message),
                    new Placeholder("displayname", sender),
                    new Placeholder("userprefix", ""),
                    new Placeholder("usersuffix", ""),
                    new Placeholder("server", ""),
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));

        } else {
            VelocityMessages.PLAYER_ONLY.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
        }
    }
}