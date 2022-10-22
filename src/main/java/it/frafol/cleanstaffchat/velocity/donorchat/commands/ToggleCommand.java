package it.frafol.cleanstaffchat.velocity.donorchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class ToggleCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public ToggleCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        if (!(DONORCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            MODULE_DISABLED.send(commandSource, new Placeholder("prefix", DONORPREFIX.color()));
            return;
        }

        if (!(commandSource instanceof Player)) {
            PLAYER_ONLY.send(commandSource, new Placeholder("prefix", DONORPREFIX.color()));
            return;
        }

        Player player = (Player) commandSource;

        if (player.hasPermission(VelocityConfig.DONORCHAT_TOGGLE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getToggled_donor().contains(player.getUniqueId())) {
                PlayerCache.getToggled_donor().add(player.getUniqueId());
                DONORCHAT_TOGGLED_OFF.send(commandSource,
                        new Placeholder("prefix", DONORPREFIX.color()));
            } else {
                PlayerCache.getToggled_donor().remove(player.getUniqueId());
                DONORCHAT_TOGGLED_ON.send(commandSource,
                        new Placeholder("prefix", DONORPREFIX.color()));
            }
        } else {
            NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", DONORPREFIX.color()));
        }
    }
}