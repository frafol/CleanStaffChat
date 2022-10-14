package it.frafol.cleanstaffchat.velocity.Listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.UpdateCheck;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;

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
                            event.getPlayer().sendMessage(Component.text(ChatColor.YELLOW + "[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz"));
                        }
                    }
                });
            }
            return;
        }
        if (!(CleanStaffChat.getInstance().getServer().getAllPlayers().size() < 1)) {
            Player player = event.getPlayer();
            if (VelocityConfig.STAFFCHAT_SWITCH_MODULE.get(Boolean.class)) {
                if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || VelocityConfig.STAFFCHAT_SWITCH_ALL.get(Boolean.class)) {
                    if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;
                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> VelocityConfig.STAFF_SWITCH_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", VelocityConfig.PREFIX.color()),
                                        new Placeholder("userprefix", user_prefix),
                                        new Placeholder("usersuffix", user_suffix),
                                        new Placeholder("displayname", user_prefix + player.getUsername() + user_suffix),
                                        new Placeholder("server", event.getServer().getServerInfo().getName())));
                    } else {
                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> VelocityConfig.STAFF_SWITCH_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", VelocityConfig.PREFIX.color()),
                                        new Placeholder("server", event.getServer().getServerInfo().getName())));
                    }
                }
            }
        }
    }
}