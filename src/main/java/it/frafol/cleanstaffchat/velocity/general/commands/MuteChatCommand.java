package it.frafol.cleanstaffchat.velocity.general.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import org.jetbrains.annotations.NotNull;

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
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;
            }

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;
            }

            PlayerCache.getMutedservers().add("all");
            VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
        }

        if (args.length == 1) {

            if (!(commandSource instanceof Player)) {
                VelocityMessages.PLAYER_ONLY.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;
            }

            if (!commandSource.hasPermission(VelocityConfig.MUTECHAT_PERMISSION.get(String.class))) {
                VelocityMessages.NO_PERMISSION.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;
            }

            String server = args[0];

            if (server.equalsIgnoreCase("all")) {

                if (!commandSource.hasPermission(VelocityConfig.MUTECHAT_ALL_PERMISSION.get(String.class))) {
                    VelocityMessages.NO_PERMISSION.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                    return;
                }

                if (PlayerCache.getMutedservers().contains("all")) {
                    PlayerCache.getMutedservers().remove("all");
                    VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                            new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                    return;
                }

                PlayerCache.getMutedservers().add("all");
                VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;
            }

            if (!PLUGIN.getServer().getServer(server).isPresent()) {
                VelocityMessages.SERVER_NOT_FOUND.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;
            }

            if (PlayerCache.getMutedservers().contains(server)) {
                PlayerCache.getMutedservers().remove(server);
                VelocityMessages.MUTECHAT_DISABLED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;
            }

            PlayerCache.getMutedservers().add(server);
            VelocityMessages.MUTECHAT_ENABLED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
        }
    }
}