package it.frafol.cleanstaffchat.velocity.reports.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;

public class ReportCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;
    public ProxyServer server;

    public ReportCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource commandSource = invocation.source();
        String[] args = invocation.arguments();

        if (!commandSource.hasPermission("test.permission")) {

            VelocityMessages.NO_PERMISSION.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;

        }

        if (args.length == 0) {

            if (!(commandSource instanceof Player)) {

                VelocityMessages.PLAYER_ONLY.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                return;

            }

            VelocityMessages.ARGUMENTS.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));

        }

        if (args.length == 1) {

            if (!PLUGIN.getServer().getAllPlayers().toString().contains(args[0])) {

                PLUGIN.getLogger().info("Giocatore non online");
                VelocityMessages.PLAYER_ONLY.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));

            }



        }

        if (args.length > 1) {



        }


    }
}
