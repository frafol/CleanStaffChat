package it.frafol.cleanstaffchat.bungee.Listeners;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.UpdateCheck;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void handle(PostLoginEvent event){
        if (event.getPlayer().hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                && (BungeeConfig.UPDATE_CHECK.get(Boolean.class))) {
            new UpdateCheck(PLUGIN).getVersion(version -> {
                if (!PLUGIN.getDescription().getVersion().equals(version)) {
                    event.getPlayer().sendMessage(new TextComponent("[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz"));
                    PLUGIN.getLogger().warning("§eThere is a new update available, download it on SpigotMC!");
                }
            });
        }
        if (!(CleanStaffChat.getInstance().getProxy().getPlayers().size() < 1)) {
            ProxiedPlayer player = event.getPlayer();
            if (BungeeConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || BungeeConfig.STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {
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
                                .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFF_JOIN_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                                        .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("%user%", player.getName()))));
                    } else {
                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFF_JOIN_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                                        .replace("%user%", player.getName()))));
                    }
                }
            }
        }
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event){
        if (CleanStaffChat.getInstance().getProxy().getPlayers().size() >= 1) {
            ProxiedPlayer player = event.getPlayer();
            if (BungeeConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || BungeeConfig.STAFFCHAT_QUIT_ALL.get(Boolean.class)) {
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
                                .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFF_QUIT_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                                        .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("%user%", player.getName()))));
                    } else {
                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(new TextComponent(BungeeConfig.STAFF_QUIT_MESSAGE_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.PREFIX.color())
                                        .replace("%user%", player.getName()))));
                    }
                }
            }
        }

        PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

    }
}
