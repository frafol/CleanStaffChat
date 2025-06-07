package it.frafol.cleanstaffchat.velocity.general.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MuteChatCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;
    public ProxyServer server;

    public MuteChatCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {

        CommandSource commandSource = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {

            if (!commandSource.hasPermission(VelocityConfig.MUTECHAT_ALL_PERMISSION.get(String.class))) {
                VelocityMessages.NO_PERMISSION.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            String user_prefix = "";
            String user_suffix = "";
            if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms") && commandSource instanceof Player) {
                final LuckPerms api = LuckPermsProvider.get();
                final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());
                if (user == null) return;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                user_prefix = prefix == null ? "" : prefix;
                user_suffix = suffix == null ? "" : suffix;
            }

            String server = "";
            String playerName = "Console";
            if (commandSource instanceof Player player) {
                if (player.getCurrentServer().isPresent()) server = player.getCurrentServer().get().getServerInfo().getName();
                playerName = player.getUsername();
            }

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                broadcastMuteChat(commandSource, "all", false);
                VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                        new Placeholder("userprefix", user_prefix),
                        new Placeholder("user", playerName),
                        new Placeholder("usersuffix", user_suffix),
                        new Placeholder("server", server));
                return;
            }

            PlayerCache.getMutedservers().add("all");
            broadcastMuteChat(commandSource, "all", true);
            VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                    new Placeholder("userprefix", user_prefix),
                    new Placeholder("user", playerName),
                    new Placeholder("usersuffix", user_suffix),
                    new Placeholder("server", server));
            return;
        }

        if (args.length == 1) {

            if (!(commandSource instanceof Player player)) {
                VelocityMessages.PLAYER_ONLY.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            String user_prefix = "";
            String user_suffix = "";
            if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {
                final LuckPerms api = LuckPermsProvider.get();
                final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());
                if (user == null) return;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                user_prefix = prefix == null ? "" : prefix;
                user_suffix = suffix == null ? "" : suffix;
            }

            if (!commandSource.hasPermission(VelocityConfig.MUTECHAT_PERMISSION.get(String.class))) {
                VelocityMessages.NO_PERMISSION.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            String server = args[0];

            if (server.equalsIgnoreCase("all")) {

                if (!commandSource.hasPermission(VelocityConfig.MUTECHAT_ALL_PERMISSION.get(String.class))) {
                    VelocityMessages.NO_PERMISSION.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                    return;
                }

                if (PlayerCache.getMutedservers().contains("all")) {
                    PlayerCache.getMutedservers().remove("all");
                    broadcastMuteChat(commandSource, server, false);
                    VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                            new Placeholder("userprefix", user_prefix),
                            new Placeholder("user", player.getUsername()),
                            new Placeholder("usersuffix", user_suffix),
                            new Placeholder("server", server));
                    return;
                }

                PlayerCache.getMutedservers().add("all");
                broadcastMuteChat(commandSource, server, true);
                VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                        new Placeholder("userprefix", user_prefix),
                        new Placeholder("user", player.getUsername()),
                        new Placeholder("usersuffix", user_suffix),
                        new Placeholder("server", server));
                return;
            }

            if (PLUGIN.getServer().getServer(server).isEmpty()) {
                VelocityMessages.SERVER_NOT_FOUND.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            if (PlayerCache.getMutedservers().contains(server)) {
                PlayerCache.getMutedservers().remove(server);
                broadcastMuteChat(commandSource, server, false);
                VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                        new Placeholder("userprefix", user_prefix),
                        new Placeholder("user", player.getUsername()),
                        new Placeholder("usersuffix", user_suffix),
                        new Placeholder("server", server));
                return;
            }

            PlayerCache.getMutedservers().add(server);
            broadcastMuteChat(commandSource, server, true);
            VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                    new Placeholder("userprefix", user_prefix),
                    new Placeholder("user", player.getUsername()),
                    new Placeholder("usersuffix", user_suffix),
                    new Placeholder("server", server));

        } else {
            VelocityMessages.MUTECHAT_USAGE.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
        }
    }

    private void broadcastMuteChat(CommandSource commandSource, String server, boolean activated) {

        String user_prefix = "";
        String user_suffix = "";
        if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms") && commandSource instanceof Player) {
            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());
            if (user == null) return;
            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;
        }

        if (activated) {
            if (server.equals("all")) {
                for (Player player : PLUGIN.getServer().getAllPlayers()) {
                    VelocityMessages.MUTECHAT_ENABLED_BC.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                            new Placeholder("userprefix", user_prefix),
                            new Placeholder("user", player.getUsername()),
                            new Placeholder("usersuffix", user_suffix),
                            new Placeholder("server", server));
                }
            } else {
                for (Player player : PLUGIN.getServer().getAllPlayers()) {
                    if (player.getCurrentServer().isEmpty()) continue;
                    if (!player.getCurrentServer().get().getServerInfo().getName().equals(server)) continue;
                    VelocityMessages.MUTECHAT_ENABLED_BC.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                            new Placeholder("userprefix", user_prefix),
                            new Placeholder("user", player.getUsername()),
                            new Placeholder("usersuffix", user_suffix),
                            new Placeholder("server", server));
                }
            }
            return;
        }

        if (server.equals("all")) {
            for (Player player : PLUGIN.getServer().getAllPlayers()) {
                VelocityMessages.MUTECHAT_DISABLED_BC.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                        new Placeholder("userprefix", user_prefix),
                        new Placeholder("user", player.getUsername()),
                        new Placeholder("usersuffix", user_suffix),
                        new Placeholder("server", server));
            }
        } else {
            for (Player player : PLUGIN.getServer().getAllPlayers()) {
                if (player.getCurrentServer().isEmpty()) continue;
                if (!player.getCurrentServer().get().getServerInfo().getName().equals(server)) continue;
                VelocityMessages.MUTECHAT_DISABLED_BC.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()),
                        new Placeholder("userprefix", user_prefix),
                        new Placeholder("user", player.getUsername()),
                        new Placeholder("usersuffix", user_suffix),
                        new Placeholder("server", server));
            }
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {

        final String[] strings = invocation.arguments();

        if (strings.length != 1) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (!(invocation.source() instanceof Player)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final List<String> serverslist = new ArrayList<>();

        for (RegisteredServer servers : PLUGIN.getServer().getAllServers()) {
            serverslist.add(servers.getServerInfo().getName());
        }

        if (serverslist.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.completedFuture(serverslist);
    }
}
