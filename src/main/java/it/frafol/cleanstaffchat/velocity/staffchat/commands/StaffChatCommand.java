package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.Arrays;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class StaffChatCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;
    public ProxyServer server;

    public StaffChatCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource commandSource = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {

            if (!(commandSource instanceof Player)) {

                ARGUMENTS.send(commandSource, new Placeholder("prefix", PREFIX.color()));

                return;

            }

            if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

                Player player = (Player) commandSource;

                if (((Player) commandSource).getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19
                        || ((Player) commandSource).getProtocolVersion() == ProtocolVersion.MINECRAFT_1_19_1) {

                    ARGUMENTS.send(commandSource, new Placeholder("prefix", PREFIX.color()));

                    return;

                }

                if (!(STAFFCHAT_TALK_MODULE.get(Boolean.class))) {

                    ARGUMENTS.send(commandSource, new Placeholder("prefix", PREFIX.color()));

                    return;

                }


                if (!PlayerCache.getToggled_2().contains(player.getUniqueId())) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        PlayerCache.getToggled_2().add(player.getUniqueId());

                        STAFFCHAT_TALK_ENABLED.send(commandSource,
                                new Placeholder("prefix", PREFIX.color()));

                        return;

                    } else {

                        ARGUMENTS.send(commandSource,
                                new Placeholder("prefix", PREFIX.color()));

                    }

                } else if (PlayerCache.getToggled_2().contains(player.getUniqueId())) {

                    PlayerCache.getToggled_2().remove(player.getUniqueId());

                    STAFFCHAT_TALK_DISABLED.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));

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

        if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

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
                                    new Placeholder("prefix", PREFIX.color()));

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
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("displayname", user_prefix + sender + user_suffix),
                                        new Placeholder("userprefix", user_prefix),
                                        new Placeholder("usersuffix", user_suffix),
                                        new Placeholder("server", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()),
                                        new Placeholder("prefix", PREFIX.color())));

                    } else {

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("displayname", sender),
                                        new Placeholder("userprefix", ""),
                                        new Placeholder("usersuffix", ""),
                                        new Placeholder("server", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()),
                                        new Placeholder("prefix", PREFIX.color())));

                    }

                    if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.STAFF_CHANNEL_ID.get(String.class));

                        assert channel != null;
                        channel.sendMessageFormat(VelocityConfig.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                        .replace("%user%", sender)
                                        .replace("%message%", message)
                                        .replace("%server%", ((Player) commandSource).getCurrentServer().get().getServer().getServerInfo().getName()))
                                .queue();

                    }

                } else if (CONSOLE_CAN_TALK.get(Boolean.class)) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("displayname", sender),
                                        new Placeholder("userprefix", ""),
                                        new Placeholder("usersuffix", ""),
                                        new Placeholder("server", ""),
                                        new Placeholder("prefix", PREFIX.color())));

                        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                            final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.STAFF_CHANNEL_ID.get(String.class));

                            assert channel != null;
                            channel.sendMessageFormat(VelocityConfig.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", sender)
                                            .replace("%message%", message)
                                            .replace("%server%", ""))
                                    .queue();

                        }

                    } else {

                        STAFFCHAT_MUTED_ERROR.send(commandSource,
                                new Placeholder("prefix", PREFIX.color()));

                    }

                    STAFFCHAT_FORMAT.send(commandSource,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("displayname", sender),
                            new Placeholder("userprefix", ""),
                            new Placeholder("usersuffix", ""),
                            new Placeholder("server", ""),
                            new Placeholder("prefix", PREFIX.color()));

                } else {

                    PLAYER_ONLY.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));

                }

            } else {

                STAFFCHAT_MUTED_ERROR.send(commandSource,
                        new Placeholder("prefix", PREFIX.color()));

            }

        } else {

            NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", PREFIX.color()));

        }
    }
}