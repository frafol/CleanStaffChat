package it.frafol.cleanstaffchat.bungee.staffchat.listeners;

import com.google.common.collect.Lists;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListChatListener extends ListenerAdapter {

    public final CleanStaffChat PLUGIN;

    public ListChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().equalsIgnoreCase("/stafflist")) {

            if (BungeeDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class).equalsIgnoreCase("none")) {
                return;
            }

            if (!event.getChannel().getId().equalsIgnoreCase(BungeeDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class))) {
                return;
            }

            LuckPerms api = LuckPermsProvider.get();
            StringBuilder sb = new StringBuilder();

            String user_prefix;
            List<UUID> list = Lists.newArrayList();

            if (!PLUGIN.getProxy().getPlayers().isEmpty()) {
                for (ProxiedPlayer players : PLUGIN.getProxy().getPlayers()) {

                    if (!players.hasPermission(BungeeConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                        continue;
                    }

                    if (BungeeConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(BungeeConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                        continue;
                    }

                    list.add(players.getUniqueId());
                }
            }

            sb.append((BungeeMessages.DISCORDLIST_HEADER.color() + "\n")
                    .replace("%online%", String.valueOf(list.size())));

            if (list.isEmpty()) {
                sb.append((BungeeMessages.DISCORDLIST_NONE.color() + "\n")
                        .replace("%prefix%", BungeeMessages.PREFIX.color()));
            }

            if (BungeeConfig.SORTING_LIST_ENABLE.get(Boolean.class)) {
                List<UUID> sortedList = new ArrayList<>();
                for (String groups : BungeeConfig.SORTING_LIST.getStringList()) {
                    for (UUID uuid : list) {
                        User user = api.getUserManager().getUser(uuid);
                        if (user == null) continue;
                        if (PLUGIN.isPremiumVanish() && BungeeVanishAPI.getInvisiblePlayers().contains(uuid)) continue;
                        Group group = api.getGroupManager().getGroup(groups);
                        if (user.getInheritedGroups(QueryOptions.builder(QueryMode.CONTEXTUAL).build()).contains(group)) {
                            sortedList.add(user.getUniqueId());
                        }
                    }
                }
                list.removeAll(sortedList);
                sortedList.addAll(list);
                list = sortedList;
            } else if (BungeeConfig.SORTING.get(Boolean.class)) {
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

                ProxiedPlayer players = PLUGIN.getProxy().getPlayer(uuids);
                User user = api.getUserManager().getUser(players.getUniqueId());

                if (user == null) {
                    continue;
                }

                Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

                String isAFK = "";
                if (PlayerCache.getAfk().contains(uuids)) {
                    isAFK = BungeeMessages.DISCORDLIST_AFK.color();
                }

                if (group == null || group.getDisplayName() == null) {

                    final String prefix = user.getCachedData().getMetaData().getPrimaryGroup();
                    if (prefix != null) {
                        user_prefix = prefix;
                    } else {
                        user_prefix = "";
                    }

                    if (players.getServer() == null) {
                        continue;
                    }

                    sb.append((BungeeMessages.DISCORDLIST_FORMAT.get(String.class) + "\n")
                            .replace("%usergroup%", PlayerCache.translateHex(user_prefix))
                            .replace("%player%", players.getName())
                            .replace("%afk%", isAFK)
                            .replace("%server%", players.getServer().getInfo().getName()));

                    continue;
                }

                final String prefix = group.getDisplayName();
                user_prefix = prefix == null ? group.getDisplayName() : prefix;

                if (players.getServer() == null) {
                    continue;
                }

                sb.append((BungeeMessages.DISCORDLIST_FORMAT.get(String.class) + "\n")
                        .replace("%usergroup%", PlayerCache.translateHex(user_prefix))
                        .replace("%player%", players.getName())
                        .replace("%afk%", isAFK)
                        .replace("%server%", players.getServer().getInfo().getName()));

            }
            sb.append(BungeeMessages.DISCORDLIST_FOOTER.get(String.class).replace("%online%", String.valueOf(list.size())));

            if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(BungeeDiscordConfig.STAFFLIST_EMBED_TITLE.get(String.class), null);
                embed.setDescription(sb.toString());
                embed.setColor(Color.getColor(BungeeDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);
                event.getChannel().sendMessageEmbeds(embed.build()).queue();

            } else {
                event.getMessage().reply(sb.toString()).queue();
            }

            return;
        }
    }
}
