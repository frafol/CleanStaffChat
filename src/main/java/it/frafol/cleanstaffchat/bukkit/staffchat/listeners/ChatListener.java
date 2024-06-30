package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.google.common.collect.Lists;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent event) {

        if (!PlayerCache.getToggled_2().contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {
            PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
            event.setCancelled(true);
            event.getPlayer().sendMessage(SpigotMessages.STAFFCHAT_MUTED_ERROR.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        if (event.getMessage().startsWith("/")) {
            return;
        }

        if (!(SpigotConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
            event.getPlayer().sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())
                    .replace("&", "§")));
        }

        if (event.getPlayer().hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

            final String message = event.getMessage();
            event.setCancelled(true);

            if (SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
                if (PlayerCache.hasColorCodes(message)) {
                    event.getPlayer().sendMessage(SpigotMessages.COLOR_CODES.color()
                            .replace("%prefix%", SpigotMessages.PREFIX.color())
                            .replace("&", "§"));
                    return;
                }
            }


            if (PLUGIN.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();
                final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                PLUGIN.getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", event.getMessage())
                                .replace("%displayname%", PlayerCache.color(user_prefix) + event.getPlayer().getName() + PlayerCache.color(user_suffix))
                                .replace("%userprefix%", PlayerCache.color(user_prefix))
                                .replace("%server%", "")
                                .replace("%usersuffix%", PlayerCache.color(user_suffix))
                                .replace("&", "§")));

            } else if (PLUGIN.getServer().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(event.getPlayer().getUniqueId()).isPresent()) {
                    return;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(event.getPlayer().getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                PLUGIN.getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + event.getPlayer().getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%message%", message)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%server%", "")
                                .replace("&", "§")));

            } else {

                PLUGIN.getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", event.getMessage())
                                .replace("%server%", "")
                                .replace("&", "§")));
            }

            if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return;
            }

            final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", event.getPlayer().getName())
                        .replace("%message%", message)
                        .replace("%server%", ""));

                embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {

                channel.sendMessageFormat(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", message)
                                .replace("%server%", ""))
                        .queue();

            }
            event.setCancelled(true);
        } else {
            PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {
            return;
        }

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

            if (SpigotConfig.SORTING.get(Boolean.class)) {
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

        if (!event.getChannel().getId().equalsIgnoreCase(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {
            TaskScheduler scheduler = UniversalScheduler.getScheduler(PLUGIN);
            scheduler.runTaskLaterAsynchronously(() -> event.getMessage().delete().queue(), 5L * 20L);
            return;
        }

        if (event.getAuthor().isBot() && !SpigotDiscordConfig.FORWARD_BOT.get(Boolean.class)) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {
            event.getMessage().reply(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();
            TaskScheduler scheduler = UniversalScheduler.getScheduler(PLUGIN);
            scheduler.runTaskLaterAsynchronously(() -> event.getMessage().delete().queue(), 5L * 20L);
            return;
        }

        PLUGIN.getServer().getOnlinePlayers().stream().filter
                        (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                .forEach(players -> players.sendMessage(SpigotMessages.DISCORD_STAFF_FORMAT.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())
                        .replace("%user%", event.getAuthor().getName())
                        .replace("%message%", event.getMessage().getContentDisplay())));
    }
}