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

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            PlayerCache.getMutedservers().add("all");
            VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
            return;
        }

        if (args.length == 1) {

            if (!(commandSource instanceof Player)) {
                VelocityMessages.PLAYER_ONLY.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
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
                    VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                    return;
                }

                PlayerCache.getMutedservers().add("all");
                VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            if (!PLUGIN.getServer().getServer(server).isPresent()) {
                VelocityMessages.SERVER_NOT_FOUND.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            if (PlayerCache.getMutedservers().contains(server)) {
                PlayerCache.getMutedservers().remove(server);
                VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
                return;
            }

            PlayerCache.getMutedservers().add(server);
            VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));

        } else {
            VelocityMessages.MUTECHAT_USAGE.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.GLOBALPREFIX.color()));
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
