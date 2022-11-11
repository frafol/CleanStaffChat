package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
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

        if (!(STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                PlayerCache.getMuted().add("true");
                VelocityMessages.STAFFCHAT_MUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            } else {
                PlayerCache.getMuted().remove("true");
                VelocityMessages.STAFFCHAT_UNMUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            }
        } else {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
        }
    }
}