package it.frafol.cleanstaffchat.velocity.donorchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class MuteCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public MuteCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();

        if (!(DONORCHAT_MUTE_MODULE.get(Boolean.class))) {
            MODULE_DISABLED.send(commandSource, new Placeholder("prefix", DONORPREFIX.color()));
            return;
        }

        if (commandSource.hasPermission(VelocityConfig.DONORCHAT_MUTE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted_donor().contains("true")) {
                PlayerCache.getMuted_donor().add("true");
                DONORCHAT_MUTED.send(commandSource,
                        new Placeholder("prefix", DONORPREFIX.color()));
            } else {
                PlayerCache.getMuted_donor().remove("true");
                DONORCHAT_UNMUTED.send(commandSource,
                        new Placeholder("prefix", DONORPREFIX.color()));
            }
        } else {
            NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", DONORPREFIX.color()));
        }
    }
}