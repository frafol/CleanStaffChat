package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.UpdateCheck;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class ServerListener {

    public final CleanStaffChat PLUGIN;

    public ServerListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void Switch(ServerConnectedEvent event) {

        if (!event.getPreviousServer().isPresent()) {

            if (event.getPlayer().hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                    && (VelocityConfig.UPDATE_CHECK.get(Boolean.class))) {

                new UpdateCheck(PLUGIN).getVersion(version -> {

                    if (PLUGIN.container.getDescription().getVersion().isPresent()) {

                        if (!PLUGIN.container.getDescription().getVersion().get().equals(version)) {

                            event.getPlayer().sendMessage(Component.text("§e[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz"));

                        }
                    }
                });
            }
            return;
        }

        if (VelocityConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)
                && PlayerCache.getAfk().contains(event.getPlayer().getUniqueId())) {

            if (event.getPlayer().hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

                if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                    assert user != null;
                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();
                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> VelocityMessages.STAFFCHAT_AFK_OFF.send(players,
                                    new Placeholder("user", event.getPlayer().getUsername()),
                                    new Placeholder("displayname", user_prefix + event.getPlayer() + user_suffix),
                                    new Placeholder("userprefix", user_prefix),
                                    new Placeholder("usersuffix", user_suffix),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                } else {

                    CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                    (players -> players.hasPermission(VelocityConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> VelocityMessages.STAFFCHAT_AFK_OFF.send(players,
                                    new Placeholder("user", event.getPlayer().getUsername()),
                                    new Placeholder("displayname", event.getPlayer().getUsername()),
                                    new Placeholder("userprefix", ""),
                                    new Placeholder("usersuffix", ""),
                                    new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                }

                PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

            }
        }

        if (!(CleanStaffChat.getInstance().getServer().getAllPlayers().size() < 1)) {

            final Player player = event.getPlayer();

            if (VelocityConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) {

                if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || VelocityConfig.STAFFCHAT_SWITCH_ALL.get(Boolean.class)) {

                    if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> VelocityMessages.STAFF_SWITCH_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                                        new Placeholder("userprefix", user_prefix),
                                        new Placeholder("usersuffix", user_suffix),
                                        new Placeholder("displayname", user_prefix + player.getUsername() + user_suffix),
                                        new Placeholder("server", event.getServer().getServerInfo().getName())));

                    } else {

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> VelocityMessages.STAFF_SWITCH_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                                        new Placeholder("server", event.getServer().getServerInfo().getName())));

                    }
                }
            }
        }
    }
}