package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import com.google.common.collect.Lists;
import de.myzelyam.api.vanish.VanishAPI;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
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

            if (!PLUGIN.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
                return;
            }

            if (SpigotDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class).equalsIgnoreCase("none")) {
                return;
            }

            if (!event.getChannel().getId().equalsIgnoreCase(SpigotDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class))) {
                return;
            }

            LuckPerms api = LuckPermsProvider.get();
            StringBuilder sb = new StringBuilder();
            String user_prefix;

            List<UUID> list = Lists.newArrayList();
            if (!PLUGIN.getServer().getOnlinePlayers().isEmpty()) {
                for (Player players : PLUGIN.getServer().getOnlinePlayers()) {
                    if (!players.hasPermission(SpigotConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                        continue;
                    }
                    if (SpigotConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(SpigotConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                        continue;
                    }
                    list.add(players.getUniqueId());
                }
            }

            sb.append((SpigotMessages.DISCORDLIST_HEADER.color() + "\n").replace("%online%", String.valueOf(list.size())));

            if (list.isEmpty()) {
                sb.append(SpigotMessages.DISCORDLIST_NONE.get(String.class)).append("\n");
            }

            if (SpigotConfig.SORTING_LIST_ENABLE.get(Boolean.class)) {
                List<UUID> sortedList = new ArrayList<>();
                for (String groups : SpigotConfig.SORTING_LIST.getStringList()) {
                    for (UUID uuid : list) {
                        User user = api.getUserManager().getUser(uuid);
                        if (user == null) continue;
                        if (PLUGIN.isPremiumVanish() && VanishAPI.getInvisiblePlayers().contains(uuid)) continue;
                        Group group = api.getGroupManager().getGroup(groups);
                        if (user.getInheritedGroups(QueryOptions.builder(QueryMode.CONTEXTUAL).build()).contains(group)) {
                            if (!sortedList.contains(user.getUniqueId())) sortedList.add(user.getUniqueId());
                        }
                    }
                }
                list.clear();
                list = sortedList;
            } else if (SpigotConfig.SORTING.get(Boolean.class)) {
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

                Player players = PLUGIN.getServer().getPlayer(uuids);

                if (players == null) {
                    continue;
                }

                User user = api.getUserManager().getUser(players.getUniqueId());

                if (user == null) {
                    continue;
                }

                Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());

                String isAFK = "";
                if (PlayerCache.getAfk().contains(uuids)) {
                    isAFK = SpigotMessages.DISCORDLIST_AFK.color();
                }

                if (group == null || group.getDisplayName() == null) {

                    final String prefix = user.getCachedData().getMetaData().getPrimaryGroup();

                    if (prefix != null) {
                        user_prefix = prefix;
                    } else {
                        user_prefix = "";
                    }

                    sb.append((SpigotMessages.DISCORDLIST_FORMAT.get(String.class) + "\n")
                            .replace("%usergroup%", PlayerCache.color(user_prefix))
                            .replace("%player%", players.getName())
                            .replace("%afk%", isAFK)
                            .replace("%server%", ""));

                    continue;
                }

                final String prefix = group.getDisplayName();
                user_prefix = prefix == null ? group.getDisplayName() : prefix;

                sb.append((SpigotMessages.DISCORDLIST_FORMAT.get(String.class) + "\n")
                        .replace("%usergroup%", PlayerCache.color(user_prefix))
                        .replace("%player%", players.getName())
                        .replace("%afk%", isAFK)
                        .replace("%server%", ""));

            }
            sb.append(SpigotMessages.DISCORDLIST_FOOTER.get(String.class).replace("%online%", String.valueOf(list.size())));

            if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(SpigotDiscordConfig.STAFFLIST_EMBED_TITLE.get(String.class), null);
                embed.setDescription(sb.toString());
                embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);
                event.getChannel().sendMessageEmbeds(embed.build()).queue();

            } else {
                event.getMessage().reply(sb.toString()).queue();
            }

            return;
        }
    }
}
