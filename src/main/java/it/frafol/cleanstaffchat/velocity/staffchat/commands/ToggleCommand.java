package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.STAFFCHAT_TOGGLE_MODULE;

public class ToggleCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public ToggleCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        if (!(STAFFCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        if (!(commandSource instanceof Player)) {
            VelocityMessages.PLAYER_ONLY.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        Player player = (Player) commandSource;

        if (!player.hasPermission(VelocityConfig.STAFFCHAT_TOGGLE_PERMISSION.get(String.class))) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        if (!PlayerCache.getToggled().contains(player.getUniqueId())) {
            PlayerCache.getToggled().add(player.getUniqueId());
            VelocityMessages.STAFFCHAT_TOGGLED_OFF.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        PlayerCache.getToggled().remove(player.getUniqueId());
        VelocityMessages.STAFFCHAT_TOGGLED_ON.send(commandSource,
                new Placeholder("prefix", VelocityMessages.PREFIX.color()));
    }
}