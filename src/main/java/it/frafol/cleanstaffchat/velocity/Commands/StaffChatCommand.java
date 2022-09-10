package it.frafol.cleanstaffchat.velocity.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import java.util.Arrays;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class StaffChatCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public StaffChatCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {

            if (!(commandSource instanceof Player)) {
                ARGUMENTS.send(commandSource, new Placeholder("prefix", PREFIX.color()));
                return;
            }

            Player player = (Player) commandSource;

            if (!(STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                MODULE_DISABLED.send(commandSource, new Placeholder("prefix", PREFIX.color()));
                return;
            }

            if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
                if (!PlayerCache.getToggled_2().contains(player.getUniqueId())) {
                    if (!PlayerCache.getMuted().contains("true")) {
                        PlayerCache.getToggled_2().add(player.getUniqueId());
                        STAFFCHAT_TALK_ENABLED.send(commandSource,
                                new Placeholder("prefix", PREFIX.color()));
                        return;
                    } else {
                        ARGUMENTS.send(commandSource,
                                new Placeholder("prefix", PREFIX.color()));
                    }
                } else if (PlayerCache.getToggled_2().contains(player.getUniqueId())) {
                    PlayerCache.getToggled_2().remove(player.getUniqueId());
                    STAFFCHAT_TALK_DISABLED.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));
                    return;
                }
            } else {
                NO_PERMISSION.send(commandSource,
                        new Placeholder("prefix", PREFIX.color()));
                return;
            }
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        String sender = !(commandSource instanceof Player) ? CONSOLE_PREFIX.get(String.class) :
        ((Player) commandSource).getUsername();

        if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                if (commandSource instanceof Player) {
                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                    new Placeholder("user", sender),
                                    new Placeholder("message", message),
                                    new Placeholder("prefix", PREFIX.color())));
                } else if (CONSOLE_CAN_TALK.get(Boolean.class)) {
                    if (!PlayerCache.getMuted().contains("true")) {
                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("prefix", PREFIX.color())));
                    } else {
                        STAFFCHAT_MUTED_ERROR.send(commandSource,
                                new Placeholder("prefix", PREFIX.color()));
                    }
                    STAFFCHAT_FORMAT.send(commandSource,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("prefix", PREFIX.color()));
                } else {
                    PLAYER_ONLY.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));
                }
            } else {
                STAFFCHAT_MUTED_ERROR.send(commandSource,
                        new Placeholder("prefix", PREFIX.color()));
            }
        } else {
            NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", PREFIX.color()));
        }
    }
}