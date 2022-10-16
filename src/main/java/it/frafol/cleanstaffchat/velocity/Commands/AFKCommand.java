package it.frafol.cleanstaffchat.velocity.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class AFKCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public AFKCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {

        CommandSource commandSource = invocation.source();

        if (!(commandSource instanceof Player)) {
            PLAYER_ONLY.send(commandSource, new Placeholder("prefix", PREFIX.color()));
            return;
        }

        if (!VelocityConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {

            MODULE_DISABLED.send(commandSource, new Placeholder("prefix", PREFIX.color()));

            return;

        }

        if (!commandSource.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

            NO_PERMISSION.send(commandSource, new Placeholder("prefix", PREFIX.color()));

            return;

        }

        if (!PlayerCache.getAfk().contains(((Player) commandSource).getUniqueId())) {
            if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> STAFFCHAT_AFK_ON.send(players,
                                new Placeholder("user", ((Player) commandSource).getUsername()),
                                new Placeholder("displayname", user_prefix + commandSource + user_suffix),
                                new Placeholder("userprefix", user_prefix),
                                new Placeholder("usersuffix", user_suffix),
                                new Placeholder("prefix", PREFIX.color())));

            } else {

                CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> STAFFCHAT_AFK_ON.send(players,
                                new Placeholder("user", ((Player) commandSource).getUsername()),
                                new Placeholder("displayname", ((Player) commandSource).getUsername()),
                                new Placeholder("userprefix", ""),
                                new Placeholder("usersuffix", ""),
                                new Placeholder("prefix", PREFIX.color())));

            }

            PlayerCache.getAfk().add(((Player) commandSource).getUniqueId());

        } else {

            if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> STAFFCHAT_AFK_OFF.send(players,
                                new Placeholder("user", ((Player) commandSource).getUsername()),
                                new Placeholder("displayname", user_prefix + commandSource + user_suffix),
                                new Placeholder("userprefix", user_prefix),
                                new Placeholder("usersuffix", user_suffix),
                                new Placeholder("prefix", PREFIX.color())));

            } else {

                CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                (players -> players.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> STAFFCHAT_AFK_OFF.send(players,
                                new Placeholder("user", ((Player) commandSource).getUsername()),
                                new Placeholder("displayname", ((Player) commandSource).getUsername()),
                                new Placeholder("userprefix", ""),
                                new Placeholder("usersuffix", ""),
                                new Placeholder("prefix", PREFIX.color())));

            }

            PlayerCache.getAfk().remove(((Player) commandSource).getUniqueId());

        }
    }
}
