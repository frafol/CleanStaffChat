package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import com.google.common.collect.Lists;
import de.myzelyam.api.vanish.VanishAPI;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StaffListCommand extends CommandBase {

    public StaffListCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {

        if (plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            if (!sender.hasPermission(SpigotConfig.STAFFLIST_PERMISSION.get(String.class))) {
                sender.sendMessage(SpigotMessages.NO_PERMISSION.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color()));
                return false;
            }

            if (args.length != 0) {
                sender.sendMessage(SpigotMessages.LIST_USAGE.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color()));
                return false;
            }

            LuckPerms api = LuckPermsProvider.get();
            String user_prefix;
            String user_suffix;

            List<UUID> list = Lists.newArrayList();
            for (Player players : plugin.getServer().getOnlinePlayers()) {

                if (!players.hasPermission(SpigotConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                    continue;
                }

                if (SpigotConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(SpigotConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                    continue;
                }

                if (plugin.isPremiumVanish() && VanishAPI.getInvisiblePlayers().contains(players.getUniqueId())) {
                    continue;
                }

                if (plugin.isSuperVanish() && VanishAPI.getInvisiblePlayers().contains(players.getUniqueId())) {
                    continue;
                }

                list.add(players.getUniqueId());
            }

            sender.sendMessage(SpigotMessages.LIST_HEADER.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())
                    .replace("%online%", String.valueOf(list.size())));

            if (list.isEmpty()) {
                sender.sendMessage(SpigotMessages.LIST_NONE.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color()));
            }

            if (SpigotConfig.SORTING_LIST_ENABLE.get(Boolean.class)) {
                List<UUID> sortedList = new ArrayList<>();
                for (String groups : SpigotConfig.SORTING_LIST.getStringList()) {
                    for (User user : api.getUserManager().getLoadedUsers()) {
                        Group group = api.getGroupManager().getGroup(groups);
                        if (user.getInheritedGroups(QueryOptions.builder(QueryMode.CONTEXTUAL).build()).contains(group)) {
                            sortedList.add(user.getUniqueId());
                        }
                    }
                }
                list.removeAll(sortedList);
                sortedList.addAll(list);
                list = sortedList;
            } else if (SpigotConfig.SORTING.get(Boolean.class)) {
                list.sort((o1, o2) -> {

                    User user1 = api.getUserManager().getUser(o1);
                    User user2 = api.getUserManager().getUser(o2);

                    Group group1 = null;
                    if (user1 != null && getHighestWeightGroup(user1).isPresent()) {
                        group1 = api.getGroupManager().getGroup(getHighestWeightGroup(user1).get());
                    }

                    Group group2 = null;
                    if (user2 != null && getHighestWeightGroup(user2).isPresent()) {
                        group2 = api.getGroupManager().getGroup(getHighestWeightGroup(user2).get());
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

            for (UUID uuids : list) {

                Player players = plugin.getServer().getPlayer(uuids);
                User user = api.getUserManager().getUser(players.getUniqueId());

                if (user == null) {
                    continue;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

                String isAFK = "";
                if (PlayerCache.getAfk().contains(uuids)) {
                    isAFK = SpigotMessages.STAFFLIST_AFK.get(String.class);
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

                    if (players.getServer() == null) {
                        continue;
                    }

                    sender.sendMessage(SpigotMessages.LIST_FORMAT.color(players)
                            .replace("%userprefix%", PlayerCache.color(user_prefix))
                            .replace("%usersuffix%", PlayerCache.color(user_suffix))
                            .replace("%player%", players.getName())
                            .replace("%server%", "")
                            .replace("%afk%", isAFK)
                            .replace("%prefix%", SpigotMessages.PREFIX.color()));

                    continue;
                }

                user_prefix = prefix == null ? group.getDisplayName() : prefix;
                user_suffix = suffix == null ? group.getDisplayName() : suffix;

                if (players.getServer() == null) {
                    continue;
                }

                sender.sendMessage(SpigotMessages.LIST_FORMAT.color(players)
                        .replace("%userprefix%", PlayerCache.color(user_prefix))
                        .replace("%usersuffix%", PlayerCache.color(user_suffix))
                        .replace("%player%", players.getName())
                        .replace("%afk%", isAFK)
                        .replace("%server%", "")
                        .replace("%prefix%", SpigotMessages.PREFIX.color()));

            }

            sender.sendMessage(SpigotMessages.LIST_FOOTER.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())
                    .replace("%online%", String.valueOf(list.size())));
            return false;
        }

        if (!sender.hasPermission(SpigotConfig.STAFFLIST_PERMISSION.get(String.class))) {
            sender.sendMessage(SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));
            return false;
        }

        if (args.length != 0) {
            sender.sendMessage(SpigotMessages.LIST_USAGE.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));
            return false;
        }

        UltraPermissionsAPI api = UltraPermissions.getAPI();
        String user_prefix;
        String user_suffix;

        List<UUID> list = Lists.newArrayList();
        for (Player players : plugin.getServer().getOnlinePlayers()) {

            if (!players.hasPermission(SpigotConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                continue;
            }

            if (SpigotConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(SpigotConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                continue;
            }

            if (plugin.isPremiumVanish() && VanishAPI.getInvisiblePlayers().contains(players.getUniqueId())) {
                continue;
            }

            list.add(players.getUniqueId());
        }

        sender.sendMessage(SpigotMessages.LIST_HEADER.color()
                .replace("%prefix%", SpigotMessages.PREFIX.color())
                .replace("%online%", String.valueOf(list.size())));

        if (list.isEmpty()) {
            sender.sendMessage(SpigotMessages.LIST_NONE.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));
        }

        if (SpigotConfig.SORTING_LIST_ENABLE.get(Boolean.class)) {
            List<UUID> sortedList = new ArrayList<>();
            for (String groups : SpigotConfig.SORTING_LIST.getStringList()) {
                for (me.TechsCode.UltraPermissions.storage.objects.User user : api.getUsers()) {
                    Optional<me.TechsCode.UltraPermissions.storage.objects.Group> group = api.getGroups().name(groups);
                    if (group.isPresent() && user.getActiveGroups().contains(group.get())) {
                        sortedList.add(user.getUuid());
                    }
                }
            }
            list.removeAll(sortedList);
            sortedList.addAll(list);
            list = sortedList;
        } else if (SpigotConfig.SORTING.get(Boolean.class)) {
            list.sort((o1, o2) -> {

                if (!api.getUsers().uuid(o1).isPresent()) {
                    return 0;
                }

                if (!api.getUsers().uuid(o2).isPresent()) {
                    return 0;
                }

                me.TechsCode.UltraPermissions.storage.objects.User user1 = api.getUsers().uuid(o1).get();
                me.TechsCode.UltraPermissions.storage.objects.User user2 = api.getUsers().uuid(o2).get();

                me.TechsCode.UltraPermissions.storage.objects.Group group1 = user1.getActiveGroups().bestToWorst().get(1);
                me.TechsCode.UltraPermissions.storage.objects.Group group2 = user2.getActiveGroups().bestToWorst().get(1);

                if (group1 == null || group2 == null) {
                    return 0;
                }

                return Integer.compare(group1.getPriority(), group2.getPriority());
            });
        }

        for (UUID uuids : list) {

            Player players = plugin.getServer().getPlayer(uuids);

            if (!api.getUsers().uuid(players.getUniqueId()).isPresent()) {
                continue;
            }

            me.TechsCode.UltraPermissions.storage.objects.User user = api.getUsers().uuid(players.getUniqueId()).get();

            String prefix = "";
            String suffix = "";

            if (user.getPrefix().isPresent()) {
                prefix = user.getPrefix().get();
            }

            if (user.getSuffix().isPresent()) {
                suffix = user.getSuffix().get();
            }

            me.TechsCode.UltraPermissions.storage.objects.Group group = user.getActiveGroups().bestToWorst().get(1);

            String isAFK = "";
            if (PlayerCache.getAfk().contains(uuids)) {
                    isAFK = SpigotMessages.STAFFLIST_AFK.color();
            }

            if (group == null || group.getName() == null) {

                user_prefix = prefix;
                user_suffix = suffix;

                sender.sendMessage(SpigotMessages.LIST_FORMAT.color()
                        .replace("%userprefix%", PlayerCache.color(user_prefix))
                        .replace("%usersuffix%", PlayerCache.color(user_suffix))
                        .replace("%player%", players.getName())
                        .replace("%server%", "")
                        .replace("%afk%", isAFK)
                        .replace("%prefix%", SpigotMessages.PREFIX.color()));

                continue;
            }

            user_prefix = prefix;
            user_suffix = suffix;

            sender.sendMessage(SpigotMessages.LIST_FORMAT.color()
                    .replace("%userprefix%", PlayerCache.color(user_prefix))
                    .replace("%usersuffix%", PlayerCache.color(user_suffix))
                    .replace("%player%", players.getName())
                    .replace("%afk%", isAFK)
                    .replace("%server%", "")
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));
        }

        sender.sendMessage(SpigotMessages.LIST_FOOTER.color()
                .replace("%prefix%", SpigotMessages.PREFIX.color())
                .replace("%online%", String.valueOf(list.size())));
        return false;
    }

    private Optional<String> getHighestWeightGroup(User user) {
        return user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> (InheritanceNode) node)
                .map(node -> LuckPermsProvider.get().getGroupManager().getGroup(node.getGroupName()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .map(Group::getName);
    }
}
