package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import com.google.common.collect.Lists;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.UUID;

public class StaffListCommand extends Command {

    private final CleanStaffChat plugin = CleanStaffChat.getInstance();

    public StaffListCommand() {
        super(BungeeCommandsConfig.STAFFLIST.getStringList().get(0),"", BungeeCommandsConfig.STAFFLIST.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission(BungeeConfig.STAFFLIST_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (args.length != 0) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LIST_USAGE.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        LuckPerms api = LuckPermsProvider.get();
        String user_prefix;
        String user_suffix;

        List<UUID> list = Lists.newArrayList();
        for (ProxiedPlayer players : plugin.getProxy().getPlayers()) {

            if (!players.hasPermission(BungeeConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                continue;
            }

            if (BungeeConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(BungeeConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                continue;
            }

            if (plugin.isPremiumVanish() && BungeeVanishAPI.getInvisiblePlayers().contains(players.getUniqueId())) {
               continue;
            }

            list.add(players.getUniqueId());

        }

        sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LIST_HEADER.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%online%", String.valueOf(list.size()))));

        if (list.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LIST_NONE.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
        }

        if (BungeeConfig.SORTING.get(Boolean.class)) {
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

        for (UUID uuids : list) {

            ProxiedPlayer players = plugin.getProxy().getPlayer(uuids);
            User user = api.getUserManager().getUser(players.getUniqueId());

            if (user == null) {
                continue;
            }

            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

            String isAFK = "";
            if (PlayerCache.getAfk().contains(uuids)) {
                isAFK = BungeeMessages.STAFFLIST_AFK.get(String.class);
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

                sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LIST_FORMAT.color()
                        .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                        .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                        .replace("%player%", players.getName())
                        .replace("%afk%", isAFK)
                        .replace("%server%", players.getServer().getInfo().getName())
                        .replace("%prefix%", BungeeMessages.PREFIX.color())));

                continue;
            }

            user_prefix = prefix == null ? group.getDisplayName() : prefix;
            user_suffix = suffix == null ? group.getDisplayName() : suffix;

            if (players.getServer() == null) {
                continue;
            }

            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LIST_FORMAT.color()
                    .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                    .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                    .replace("%player%", players.getName())
                    .replace("%afk%", isAFK)
                    .replace("%server%", players.getServer().getInfo().getName())
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));


        }
        sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.LIST_FOOTER.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())
                .replace("%online%", String.valueOf(list.size()))));
    }
}
