package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

public class StaffListCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;
    public ProxyServer server;

    public StaffListCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {

        if (!invocation.source().hasPermission(VelocityConfig.STAFFLIST_PERMISSION.get(String.class))) {
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 0) {

            LuckPerms api = LuckPermsProvider.get();

            VelocityMessages.LIST_HEADER.send(invocation.source(),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));

            String user_prefix;

            for (Player players : PLUGIN.getServer().getAllPlayers()) {

                if (players.hasPermission(VelocityConfig.STAFFLIST_PERMISSION.get(String.class))) {

                    User user = api.getUserManager().getUser(players.getUniqueId());

                    if (user == null) {
                        continue;
                    }

                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

                    if (group == null || group.getDisplayName() == null) {

                        if (prefix != null) {
                            user_prefix = prefix;
                        } else {
                            user_prefix = "";
                        }

                        if (!players.getCurrentServer().isPresent()) {
                            continue;
                        }

                        VelocityMessages.LIST_FORMAT.send(invocation.source(),
                                new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                                new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                                new Placeholder("player", players.getUsername()),
                                new Placeholder("server", players.getCurrentServer().get().getServerInfo().getName()));

                        continue;
                    }

                    user_prefix = prefix == null ? group.getDisplayName() : prefix;

                    if (!players.getCurrentServer().isPresent()) {
                        continue;
                    }

                    VelocityMessages.LIST_FORMAT.send(invocation.source(),
                            new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                            new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                            new Placeholder("player", players.getUsername()),
                            new Placeholder("server", players.getCurrentServer().get().getServerInfo().getName()));

                }
            }

            VelocityMessages.LIST_FOOTER.send(invocation.source(),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
        }
    }
}
