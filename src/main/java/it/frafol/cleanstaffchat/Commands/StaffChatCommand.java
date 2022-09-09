package it.frafol.cleanstaffchat.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.CleanStaffChat;
import it.frafol.cleanstaffchat.enums.VelocityConfig;
import it.frafol.cleanstaffchat.objects.Placeholder;
import it.frafol.cleanstaffchat.objects.PlayerCache;
import it.frafol.cleanstaffchat.objects.TextFile;

import java.util.Arrays;

import static it.frafol.cleanstaffchat.enums.VelocityConfig.*;

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
                    PlayerCache.getToggled_2().add(player.getUniqueId());
                    STAFFCHAT_TALK_ENABLED.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));
                    return;
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

        if (args[0].equalsIgnoreCase("help")) {
            ARGUMENTS.send(commandSource,
                    new Placeholder("prefix", PREFIX.color()));
            return;
        }

        if (DEBUG.get(Boolean.class)) {
            PLUGIN.getLogger().info("[DEBUG] First IF passed");
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
                TextFile.reloadAll();
                RELOADED.send(commandSource,
                        new Placeholder("prefix", PREFIX.color()));
                return;
            } else {
                NO_PERMISSION.send(commandSource,
                        new Placeholder("prefix", PREFIX.color()));
            }
        }

        if (DEBUG.get(Boolean.class)) {
            PLUGIN.getLogger().info("[DEBUG] Second IF passed");
        }


        if (args[0].equalsIgnoreCase("toggle")) {

            if (!(STAFFCHAT_TOGGLE_MODULE.get(Boolean.class))) {
                MODULE_DISABLED.send(commandSource, new Placeholder("prefix", PREFIX.color()));
                return;
            }

            if (!(commandSource instanceof Player)) {
                PLAYER_ONLY.send(commandSource, new Placeholder("prefix", PREFIX.color()));
                return;
            }

            Player player = (Player) commandSource;

            if (player.hasPermission(VelocityConfig.STAFFCHAT_TOGGLE_PERMISSION.get(String.class))) {
                if (!PlayerCache.getToggled().contains(player.getUniqueId())) {
                    PlayerCache.getToggled().add(player.getUniqueId());
                    STAFFCHAT_TOGGLED_OFF.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));
                    return;
                }
            }

            PlayerCache.getToggled().remove(player.getUniqueId());

            STAFFCHAT_TOGGLED_ON.send(commandSource,
                    new Placeholder("prefix", PREFIX.color()));

            return;
        }

        if (DEBUG.get(Boolean.class)) {
            PLUGIN.getLogger().info("[DEBUG] Third IF passed");
        }

        if (args[0].equalsIgnoreCase("mute")) {

            if (!(STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
                MODULE_DISABLED.send(commandSource, new Placeholder("prefix", PREFIX.color()));
                return;
            }

            if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {
                if (!PlayerCache.getMuted().contains("true")) {
                    PlayerCache.getMuted().add("true");
                    STAFFCHAT_MUTED.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));
                } else {
                    PlayerCache.getMuted().remove("true");
                    STAFFCHAT_UNMUTED.send(commandSource,
                            new Placeholder("prefix", PREFIX.color()));
                }
                return;
            }
        }

        if (DEBUG.get(Boolean.class)) {
            PLUGIN.getLogger().info("[DEBUG] Fourth IF passed");
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        if (DEBUG.get(Boolean.class)) {
            PLUGIN.getLogger().info("Message: " + message);
        }

        String sender = !(commandSource instanceof Player) ? CONSOLE_PREFIX.get(String.class) :
        ((Player) commandSource).getUsername();

        if (DEBUG.get(Boolean.class)) {
            PLUGIN.getLogger().info("Sender: " + sender);
        }

        if (commandSource.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted().contains("true")) {
                if (DEBUG.get(Boolean.class)) {
                    PLUGIN.getLogger().info("[DEBUG] Deciding whether the commandSender is a player or the console...");
                }
                if (commandSource instanceof Player) {
                    if (DEBUG.get(Boolean.class)) {
                        PLUGIN.getLogger().info("[DEBUG] Player detected");
                    }
                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                    new Placeholder("user", sender),
                                    new Placeholder("message", message),
                                    new Placeholder("prefix", PREFIX.color())));
                    if (DEBUG.get(Boolean.class)) {
                        PLUGIN.getLogger().info("Message was sent to " + CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId()))));
                    }
                } else if (CONSOLE_CAN_TALK.get(Boolean.class)) {
                    if (!PlayerCache.getMuted().contains("true")) {
                        if (DEBUG.get(Boolean.class)) {
                            PLUGIN.getLogger().info("[DEBUG] Console detected");
                        }
                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                        new Placeholder("user", sender),
                                        new Placeholder("message", message),
                                        new Placeholder("prefix", PREFIX.color())));
                        if (DEBUG.get(Boolean.class)) {
                            PLUGIN.getLogger().info("Message was sent to " + CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId()))));
                        }
                    } else {
                        STAFFCHAT_MUTED_ERROR.send(commandSource,
                                new Placeholder("prefix", PREFIX.color()));
                    }
                    STAFFCHAT_FORMAT.send(commandSource,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("prefix", PREFIX.color()));
                    if (DEBUG.get(Boolean.class)) {
                        PLUGIN.getLogger().info("Message was sent to " + commandSource);
                    }
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