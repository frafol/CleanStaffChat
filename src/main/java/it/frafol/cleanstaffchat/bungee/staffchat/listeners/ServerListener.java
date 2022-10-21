package it.frafol.cleanstaffchat.bungee.staffchat.listeners;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {
    public final CleanStaffChat PLUGIN;

    public ServerListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void Switch(ServerSwitchEvent event){

        if (event.getFrom() == null) {
            return;
        }

        if (BungeeConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)
                && PlayerCache.getAfk().contains(event.getPlayer().getUniqueId())) {

            if (event.getPlayer().hasPermission(BungeeConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

                if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                    final LuckPerms api = LuckPermsProvider.get();

                    final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                    assert user != null;
                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();
                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.STAFFCHAT_AFK_ON.color()
                                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                                    .replace("%user%", event.getPlayer().getName())
                                    .replace("%displayname%", user_prefix + event.getPlayer().getName() + user_suffix)
                                    .replace("%userprefix%", user_prefix)
                                    .replace("%usersuffix%", user_suffix))));

                } else {

                    CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                    (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                            && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                            .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.STAFFCHAT_AFK_ON.color()
                                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                                    .replace("%user%", event.getPlayer().getName())
                                    .replace("%userprefix%", "")
                                    .replace("%usersuffix%", "")
                                    .replace("%displayname%", event.getPlayer().getName()))));

                }

                PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

            }
        }

        if (!(CleanStaffChat.getInstance().getProxy().getPlayers().size() < 1)) {

            ProxiedPlayer player = event.getPlayer();

            if (BungeeConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) {

                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || BungeeConfig.STAFFCHAT_SWITCH_ALL.get(Boolean.class)) {

                    if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.STAFF_SWITCH_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                                        .replace("%user%", player.getName())
                                        .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("%server%", player.getServer().getInfo().getName()))));

                    } else {

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.STAFF_SWITCH_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                                        .replace("%user%", player.getName())
                                        .replace("%server%", player.getServer().getInfo().getName()))));

                    }
                }
            }
        }
    }
}
