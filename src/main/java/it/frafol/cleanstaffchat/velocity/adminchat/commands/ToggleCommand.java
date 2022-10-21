package it.frafol.cleanstaffchat.velocity.adminchat.commands;

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
        if (!(ADMINCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            MODULE_DISABLED.send(commandSource, new Placeholder("prefix", ADMINPREFIX.color()));
            return;
        }

        if (!(commandSource instanceof Player)) {
            PLAYER_ONLY.send(commandSource, new Placeholder("prefix", ADMINPREFIX.color()));
            return;
        }

        Player player = (Player) commandSource;

        if (player.hasPermission(VelocityConfig.ADMINCHAT_TOGGLE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getToggled_admin().contains(player.getUniqueId())) {
                PlayerCache.getToggled_admin().add(player.getUniqueId());
                ADMINCHAT_TOGGLED_OFF.send(commandSource,
                        new Placeholder("prefix", ADMINPREFIX.color()));
            } else {
                PlayerCache.getToggled_admin().remove(player.getUniqueId());
                ADMINCHAT_TOGGLED_ON.send(commandSource,
                        new Placeholder("prefix", ADMINPREFIX.color()));
            }
        } else {
            NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", ADMINPREFIX.color()));
        }
    }
}