package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.*;
import it.frafol.cleanstaffchat.velocity.objects.DiscordBridge;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import it.frafol.cleanstaffchat.velocity.utils.VanishUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

/** Answers the "/stafflist" message written in the Discord stafflist channel. */
public class StaffListDiscordListener extends ListenerAdapter {

    public final CleanStaffChat PLUGIN;

    public StaffListDiscordListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (PLUGIN.getConfigTextFile() == null || PLUGIN.getMessagesTextFile() == null) return;
        if (!event.getMessage().getContentDisplay().equalsIgnoreCase("/stafflist")) return;
        if (instance.getServer().getPluginManager().getPlugin("luckperms").isEmpty()) return;
        if (VelocityDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class).equalsIgnoreCase("none")) return;
        if (!event.getChannel().getId().equalsIgnoreCase(VelocityDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class))) return;
        LuckPerms api = LuckPermsProvider.get();
        StringBuilder sb = new StringBuilder();
        String user_prefix;
        List<UUID> list = Lists.newArrayList();
        if (!PLUGIN.getServer().getAllPlayers().isEmpty()) {
            for (Player players : PLUGIN.getServer().getAllPlayers()) {
                if (!players.hasPermission(VelocityConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) continue;
                if (VelocityConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(VelocityConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class)))
                    continue;
                list.add(players.getUniqueId());
            }
        }

        sb.append((VelocityMessages.DISCORDLIST_HEADER.get(String.class) + "\n").replace("%online%", String.valueOf(list.size())));
        if (list.isEmpty()) sb.append(VelocityMessages.DISCORDLIST_NOBODY.get(String.class)).append("\n");

        if (VelocityConfig.SORTING_LIST_ENABLE.get(Boolean.class)) {
            List<UUID> sortedList = new ArrayList<>();
            for (String groups : VelocityConfig.SORTING_LIST.getStringList()) {
                for (UUID uuid : list) {
                    User user = api.getUserManager().getUser(uuid);
                    if (user == null) continue;
                    if (PLUGIN.isPremiumVanish() && VanishUtil.isVanished(uuid)) continue;
                    Group group = api.getGroupManager().getGroup(groups);
                    if (user.getInheritedGroups(QueryOptions.builder(QueryMode.CONTEXTUAL).build()).contains(group)) {
                        if (!sortedList.contains(user.getUniqueId()) && list.contains(user.getUniqueId())) {
                            sortedList.add(user.getUniqueId());
                        }
                    }
                }
            }
            list.removeAll(sortedList);
            sortedList.addAll(list);
            list = sortedList;
        } else if (VelocityConfig.SORTING.get(Boolean.class)) {
            list.sort((o1, o2) -> {
                User user1 = api.getUserManager().getUser(o1);
                User user2 = api.getUserManager().getUser(o2);
                Group group1 = null;
                if (user1 != null) group1 = api.getGroupManager().getGroup(user1.getPrimaryGroup());
                Group group2 = null;
                if (user2 != null) group2 = api.getGroupManager().getGroup(user2.getPrimaryGroup());
                if (group1 == null || group2 == null) return 0;
                if (group1.getWeight().isEmpty() || group2.getWeight().isEmpty()) return 0;
                return Integer.compare(group1.getWeight().getAsInt(), group2.getWeight().getAsInt());
            });
        }

        for (UUID uuids : list) {
            Player players = PLUGIN.getServer().getPlayer(uuids).orElse(null);
            if (players == null) continue;
            User user = api.getUserManager().getUser(players.getUniqueId());
            if (user == null) continue;
            Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
            String isAFK = "";
            if (PlayerCache.getAfk().contains(uuids)) isAFK = VelocityMessages.DISCORDLIST_AFK.get(String.class);
            if (group == null || group.getDisplayName() == null) {
                final String prefix = user.getCachedData().getMetaData().getPrimaryGroup();
                user_prefix = Objects.requireNonNullElse(prefix, "");
                if (players.getCurrentServer().isEmpty()) continue;
                sb.append((VelocityMessages.DISCORDLIST_FORMAT.get(String.class) + "\n")
                        .replace("%usergroup%", ChatUtil.translateHex(user_prefix))
                        .replace("%player%", players.getUsername())
                        .replace("%afk%", isAFK)
                        .replace("%server%", players.getCurrentServer().get().getServerInfo().getName()));
                continue;
            }

            final String prefix = group.getDisplayName();
            user_prefix = prefix == null ? group.getDisplayName() : prefix;
            if (players.getCurrentServer().isEmpty()) continue;
            sb.append((VelocityMessages.DISCORDLIST_FORMAT.get(String.class) + "\n")
                    .replace("%usergroup%", ChatUtil.translateHex(user_prefix))
                    .replace("%player%", players.getUsername())
                    .replace("%afk%", isAFK)
                    .replace("%server%", players.getCurrentServer().get().getServerInfo().getName()));
        }

        sb.append(VelocityMessages.DISCORDLIST_FOOTER.get(String.class).replace("%online%", String.valueOf(list.size())));
        if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(VelocityDiscordConfig.STAFFLIST_EMBED_TITLE.get(String.class), null);
            embed.setDescription(sb.toString());
            embed.setColor(DiscordBridge.parseColor(VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
            embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        } else event.getMessage().reply(sb.toString()).queue();
    }
}
