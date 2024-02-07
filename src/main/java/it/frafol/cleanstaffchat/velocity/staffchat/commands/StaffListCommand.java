package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.google.common.collect.Lists;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.enums.VelocityRedis;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import it.frafol.cleanstaffchat.velocity.utils.VanishUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class StaffListCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;
    public ProxyServer server;

    public StaffListCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {

        if (!invocation.source().hasPermission(VelocityConfig.STAFFLIST_PERMISSION.get(String.class))) {
            VelocityMessages.NO_PERMISSION.send(invocation.source(),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length != 0) {
            VelocityMessages.LIST_USAGE.send(invocation.source(),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        LuckPerms api = LuckPermsProvider.get();
        String user_prefix;
        String user_suffix;

        List<UUID> list = Lists.newArrayList();

        if (PLUGIN.getServer().getPluginManager().getPlugin("redisbungee").isPresent() && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {
            list = handleRedis();
        } else {
            for (Player players : PLUGIN.getServer().getAllPlayers()) {

                if (!players.hasPermission(VelocityConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                    continue;
                }

                if (VelocityConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(VelocityConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                    continue;
                }

                if (PLUGIN.isPremiumVanish() && VanishUtil.isVanished(players)) {
                    continue;
                }

                list.add(players.getUniqueId());
            }
        }

        VelocityMessages.LIST_HEADER.send(invocation.source(),
                new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                new Placeholder("online", String.valueOf(list.size())));

        if (list.isEmpty()) {
            VelocityMessages.LIST_NONE.send(invocation.source(),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
        }

        if (VelocityConfig.SORTING.get(Boolean.class)) {
            list.sort((o1, o2) -> {

                User user1 = api.getUserManager().getUser(o1);
                User user2 = api.getUserManager().getUser(o2);

                Group group1 = null;
                if (user1 != null) {
                    group1 = api.getGroupManager().getGroup(user1.getPrimaryGroup());
                }

                Group group2 = null;
                if (user2 != null) {
                    group2 = api.getGroupManager().getGroup(user2.getPrimaryGroup());
                }

                if (group1 == null || group2 == null) {
                    return 0;
                }

                if (!group1.getWeight().isPresent() || !group2.getWeight().isPresent()) {
                    return 0;
                }

                return Integer.compare(group1.getWeight().getAsInt(), group2.getWeight().getAsInt());
            });
        }

        if (VelocityRedis.REDIS_ENABLE.get(Boolean.class) && PLUGIN.getServer().getPluginManager().getPlugin("redisbungee").isPresent()) {
            sendRedisList(invocation, list, api);
        } else for (UUID uuids : list) {

            Player players = PLUGIN.getServer().getPlayer(uuids).orElse(null);

            if (players == null) {
                continue;
            }

            User user = api.getUserManager().getUser(players.getUniqueId());

            if (user == null) {
                continue;
            }

            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

            String isAFK = "";
            if (PlayerCache.getAfk().contains(uuids)) {
                isAFK = VelocityMessages.DISCORDLIST_AFK.get(String.class);
            }

            if (group == null || group.getDisplayName() == null) {

                if (prefix != null) {
                    user_prefix = prefix;
                } else {
                    user_prefix = "";
                }

                if (suffix != null) {
                    user_suffix = suffix;
                } else {
                    user_suffix = "";
                }

                if (!players.getCurrentServer().isPresent()) {
                    continue;
                }

                VelocityMessages.LIST_FORMAT.send(invocation.source(),
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                        new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                        new Placeholder("afk", isAFK),
                        new Placeholder("player", players.getUsername()),
                        new Placeholder("server", players.getCurrentServer().get().getServerInfo().getName()));

                continue;
            }

            user_prefix = prefix == null ? group.getDisplayName() : prefix;
            user_suffix = suffix == null ? group.getDisplayName() : suffix;

            if (!players.getCurrentServer().isPresent()) {
                continue;
            }

            VelocityMessages.LIST_FORMAT.send(invocation.source(),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                    new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                    new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                    new Placeholder("afk", isAFK),
                    new Placeholder("player", players.getUsername()),
                    new Placeholder("server", players.getCurrentServer().get().getServerInfo().getName()));

        }

        VelocityMessages.LIST_FOOTER.send(invocation.source(),
                new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                new Placeholder("online", String.valueOf(list.size())));
    }

    private void sendRedisList(Invocation invocation, List<UUID> list, LuckPerms api) {
        String user_prefix;
        String user_suffix;
        RedisBungeeAPI redisApi = RedisBungeeAPI.getRedisBungeeApi();
        for (UUID uuids : list) {

            User user = api.getUserManager().getUser(uuids);

            if (user == null) {
                continue;
            }

            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

            String isAFK = "";
            if (PlayerCache.getAfk().contains(uuids)) {
                isAFK = VelocityMessages.DISCORDLIST_AFK.get(String.class);
            }

            if (group == null || group.getDisplayName() == null) {

                if (prefix != null) {
                    user_prefix = prefix;
                } else {
                    user_prefix = "";
                }

                if (suffix != null) {
                    user_suffix = suffix;
                } else {
                    user_suffix = "";
                }

                if (redisApi.getServerFor(uuids) == null || redisApi.getNameFromUuid(uuids) == null || redisApi.getServerNameFor(uuids) == null) {
                    continue;
                }

                VelocityMessages.LIST_FORMAT.send(invocation.source(),
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                        new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                        new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                        new Placeholder("afk", isAFK),
                        new Placeholder("player", redisApi.getNameFromUuid(uuids)),
                        new Placeholder("server", redisApi.getServerNameFor(uuids)));

                continue;
            }

            user_prefix = prefix == null ? group.getDisplayName() : prefix;
            user_suffix = suffix == null ? group.getDisplayName() : suffix;

            if (redisApi.getServerFor(uuids) == null || redisApi.getNameFromUuid(uuids) == null || redisApi.getServerNameFor(uuids) == null) {
                continue;
            }

            VelocityMessages.LIST_FORMAT.send(invocation.source(),
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                    new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                    new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                    new Placeholder("afk", isAFK),
                    new Placeholder("player", redisApi.getNameFromUuid(uuids)),
                    new Placeholder("server", redisApi.getServerNameFor(uuids)));
        }
    }

    private List<UUID> handleRedis() {
        List<UUID> list = Lists.newArrayList();
        final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
        for (UUID players : redisBungeeAPI.getPlayersOnline()) {

            LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(players);

            if (user == null) {
                continue;
            }

            if (!user.getCachedData().getPermissionData().checkPermission(VelocityConfig.STAFFLIST_SHOW_PERMISSION.get(String.class)).asBoolean()) {
                continue;
            }

            if (VelocityConfig.STAFFLIST_BYPASS.get(Boolean.class) && user.getCachedData().getPermissionData().checkPermission(VelocityConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class)).asBoolean()) {
                continue;
            }

            if (PLUGIN.isPremiumVanish() && VelocityVanishAPI.getInvisiblePlayers().contains(players)) {
                continue;
            }

            list.add(players);
        }
        return list;
    }
}
