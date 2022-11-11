package it.frafol.cleanstaffchat.velocity.adminchat.commands;

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

        if (!(ADMINCHAT_MUTE_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(commandSource, new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            return;
        }

        if (commandSource.hasPermission(VelocityConfig.ADMINCHAT_MUTE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                PlayerCache.getMuted().add("true");
                VelocityMessages.ADMINCHAT_MUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            } else {
                PlayerCache.getMuted().remove("true");
                VelocityMessages.ADMINCHAT_UNMUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            }
        } else {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
        }
    }
}