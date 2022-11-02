package it.frafol.cleanstaffchat.velocity.adminchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
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

                ADMINARGUMENTS.send(commandSource, new Placeholder("prefix", ADMINPREFIX.color()));

                return;

            }

            if (commandSource.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) {

                Player player = (Player) commandSource;

                if (((Player) commandSource).getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19
                        || ((Player) commandSource).getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19_1) {

                    ADMINARGUMENTS.send(commandSource, new Placeholder("prefix", ADMINPREFIX.color()));

                    return;

                }

                if (!(ADMINCHAT_TALK_MODULE.get(Boolean.class))) {

                    ADMINARGUMENTS.send(commandSource, new Placeholder("prefix", ADMINPREFIX.color()));

                    return;

                }


                if (!PlayerCache.getToggled_2_admin().contains(player.getUniqueId())) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        PlayerCache.getToggled_2_admin().add(player.getUniqueId());

                        ADMINCHAT_TALK_ENABLED.send(commandSource,
                                new Placeholder("prefix", ADMINPREFIX.color()));

                        return;

                    } else {

                        ADMINARGUMENTS.send(commandSource,
                                new Placeholder("prefix", ADMINPREFIX.color()));

                    }

                } else if (PlayerCache.getToggled_2_admin().contains(player.getUniqueId())) {

                    PlayerCache.getToggled_2_admin().remove(player.getUniqueId());

                    ADMINCHAT_TALK_DISABLED.send(commandSource,
                            new Placeholder("prefix", ADMINPREFIX.color()));

                    return;

                }

            } else {

                commandSource.sendMessage(Component.text("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));

                return;

            }
        }

        final String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        final String sender = !(commandSource instanceof Player) ? CONSOLE_PREFIX.get(String.class) :
        ((Player) commandSource).getUsername();

        if (commandSource.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) {

            if (!PlayerCache.getMuted().contains("true")) {

                if (commandSource instanceof Player) {

                    if (PREVENT_COLOR_CODES.get(Boolean.class)) {
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

                            COLOR_CODES.send(commandSource,
                                    new Placeholder("prefix", ADMINPREFIX.color()));

                            return;

                        }
                    }

                    if (!((Player) commandSource).getCurrentServer().isPresent()) {

                        return;

                    }

                    if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());

                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_admin().contains(players.getUniqueId())))
                                .forEach(players -> ADMINCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("displayname", user_prefix + sender + user_suffix),
                                        new Placeholder("userprefix", user_prefix),
                                        new Placeholder("usersuffix", user_suffix),
                                        new Placeholder("server", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()),
                                        new Placeholder("prefix", ADMINPREFIX.color())));

                    } else {

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_admin().contains(players.getUniqueId())))
                                .forEach(players -> ADMINCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("displayname", sender),
                                        new Placeholder("userprefix", ""),
                                        new Placeholder("usersuffix", ""),
                                        new Placeholder("server", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()),
                                        new Placeholder("prefix", ADMINPREFIX.color())));

                    }

                    if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.ADMIN_CHANNEL_ID.get(String.class));

                        assert channel != null;

                        if (VelocityConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(VelocityConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(VelocityConfig.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", sender)
                                    .replace("%message%", message)
                                    .replace("%server%", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()));

                            embed.setColor(Color.RED);
                            embed.setFooter("Powered by CleanStaffChat");

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {

                            channel.sendMessageFormat(VelocityConfig.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", sender)
                                            .replace("%message%", message)
                                            .replace("%server%", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()))
                                    .queue();

                        }
                    }

                } else if (CONSOLE_CAN_TALK.get(Boolean.class)) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_admin().contains(players.getUniqueId())))
                                .forEach(players -> ADMINCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("displayname", sender),
                                        new Placeholder("userprefix", ""),
                                        new Placeholder("usersuffix", ""),
                                        new Placeholder("server", ""),
                                        new Placeholder("prefix", ADMINPREFIX.color())));

                        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {

                            final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.ADMIN_CHANNEL_ID.get(String.class));

                            assert channel != null;

                            if (VelocityConfig.USE_EMBED.get(Boolean.class)) {

                                EmbedBuilder embed = new EmbedBuilder();

                                embed.setTitle(VelocityConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

                                embed.setDescription(VelocityConfig.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                        .replace("%user%", sender)
                                        .replace("%message%", message)
                                        .replace("%server%", ""));

                                embed.setColor(Color.RED);
                                embed.setFooter("Powered by CleanStaffChat");

                                channel.sendMessageEmbeds(embed.build()).queue();

                            } else {

                                channel.sendMessageFormat(VelocityConfig.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                                .replace("%user%", sender)
                                                .replace("%message%", message)
                                                .replace("%server%", ""))
                                        .queue();

                            }
                        }

                    } else {

                        ADMINCHAT_MUTED_ERROR.send(commandSource,
                                new Placeholder("prefix", ADMINPREFIX.color()));

                    }

                    ADMINCHAT_FORMAT.send(commandSource,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("displayname", sender),
                            new Placeholder("userprefix", ""),
                            new Placeholder("usersuffix", ""),
                            new Placeholder("server", ""),
                            new Placeholder("prefix", ADMINPREFIX.color()));

                } else {

                    PLAYER_ONLY.send(commandSource,
                            new Placeholder("prefix", ADMINPREFIX.color()));

                }

            } else {

                ADMINCHAT_MUTED_ERROR.send(commandSource,
                        new Placeholder("prefix", ADMINPREFIX.color()));

            }

        } else {

            NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", ADMINPREFIX.color()));

        }
    }
}