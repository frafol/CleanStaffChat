package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.google.common.collect.Lists;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.*;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class ChatListener extends ListenerAdapter {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {

        final String message = event.getMessage();
        final String sender = event.getPlayer().getUsername();

        if (PlayerCache.getToggled_2().contains(event.getPlayer().getUniqueId())) {

            if (event.getPlayer().hasPermission(STAFFCHAT_USE_PERMISSION.get(String.class))) {

                if (!(STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                    VelocityMessages.MODULE_DISABLED.send(event.getPlayer(), new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                    return;
                }

                if (!event.getMessage().startsWith("/")) {

                    if (!PlayerCache.getMuted().contains("true")) {
                        if (PREVENT_COLOR_CODES.get(Boolean.class)) {
                            if (ChatUtil.hasColorCodes(message)) {
                                VelocityMessages.COLOR_CODES.send(event.getPlayer(),
                                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                                return;
                            }
                        }

                        if (!(event.getPlayer().getCurrentServer().isPresent())) {
                            return;
                        }

                        if (VelocityServers.STAFFCHAT_ENABLE.get(Boolean.class)) {
                            for (String server : VelocityServers.SC_BLOCKED_SRV.getStringList()) {
                                if (event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName().equalsIgnoreCase(server)) {
                                    PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
                                    event.setResult(PlayerChatEvent.ChatResult.denied());
                                    ChatUtil.sendChannelMessage(event.getPlayer(), false);
                                    VelocityMessages.STAFFCHAT_MUTED_ERROR.send(event.getPlayer(), new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                                    return;
                                }
                            }
                        }

                        if (!VelocityConfig.DOUBLE_MESSAGE.get(Boolean.class)) {
                            event.setResult(PlayerChatEvent.ChatResult.denied());
                        }

                        if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                            LuckPerms api = LuckPermsProvider.get();
                            User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                            if (user == null) {
                                return;
                            }

                            final String prefix = user.getCachedData().getMetaData().getPrefix();
                            final String suffix = user.getCachedData().getMetaData().getSuffix();
                            final String user_prefix = prefix == null ? "" : prefix;
                            final String user_suffix = suffix == null ? "" : suffix;

                            if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class))  {

                                final String final_message = VelocityMessages.STAFFCHAT_FORMAT.get(String.class)
                                        .replace("%user%", sender)
                                        .replace("%message%", message)
                                        .replace("%displayname%", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix))
                                        .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                                        .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                                        .replace("%server%", event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName())
                                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                                        .replace("&", "§");


                                final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
                                redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffChatMessage-RedisBungee", final_message);
                                return;
                            }

                            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                            (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                    && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                                    && !instance.isInBlockedStaffChatServer(players))
                                    .forEach(players -> VelocityMessages.STAFFCHAT_FORMAT.send(players,
                                            new Placeholder("user", sender),
                                            new Placeholder("message", message),
                                            new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix)),
                                            new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                                            new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                                            new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                        } else {

                            if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                                final String final_message = VelocityMessages.STAFFCHAT_FORMAT.get(String.class)
                                        .replace("%user%", sender)
                                        .replace("%message%", message)
                                        .replace("%displayname%", sender)
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%server%", event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName())
                                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                                        .replace("&", "§");

                                final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
                                redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffMessage-RedisBungee", final_message);
                                return;
                            }

                            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                            (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                    && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                                    && !instance.isInBlockedStaffChatServer(players))
                                    .forEach(players -> VelocityMessages.STAFFCHAT_FORMAT.send(players,
                                            new Placeholder("user", sender),
                                            new Placeholder("message", message),
                                            new Placeholder("displayname", sender),
                                            new Placeholder("userprefix", ""),
                                            new Placeholder("usersuffix", ""),
                                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                                            new Placeholder("prefix", VelocityMessages.PREFIX.color())));

                        }

                        if (VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                            final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                            if (channel == null) {
                                return;
                            }

                            if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

                                EmbedBuilder embed = new EmbedBuilder();

                                embed.setTitle(VelocityDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                                embed.setDescription(VelocityMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                        .replace("%user%", sender)
                                        .replace("%message%", message)
                                        .replace("%server%", event.getPlayer().getCurrentServer().get().getServerInfo().getName()));

                                embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                                embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                                channel.sendMessageEmbeds(embed.build()).queue();

                            } else {

                                channel.sendMessageFormat(VelocityMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                                .replace("%user%", sender)
                                                .replace("%message%", message)
                                                .replace("%server%", event.getPlayer().getCurrentServer().get().getServerInfo().getName()))
                                        .queue();

                            }
                        }

                    } else {
                        event.setResult(PlayerChatEvent.ChatResult.denied());
                        VelocityMessages.STAFFCHAT_MUTED_ERROR.send(event.getPlayer(),
                                new Placeholder("prefix", VelocityMessages.PREFIX.color()));
                    }
                }

            } else {
                ChatUtil.sendChannelMessage(event.getPlayer(), false);
                PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());
            }
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null || PLUGIN.getMessagesTextFile() == null) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase("/stafflist")) {

            if (VelocityDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class).equalsIgnoreCase("none")) {
                return;
            }

            if (!event.getChannel().getId().equalsIgnoreCase(VelocityDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class))) {
                return;
            }

            LuckPerms api = LuckPermsProvider.get();
            StringBuilder sb = new StringBuilder();



            String user_prefix;
            List<UUID> list = Lists.newArrayList();

            if (!PLUGIN.getServer().getAllPlayers().isEmpty()) {
                for (Player players : PLUGIN.getServer().getAllPlayers()) {

                    if (!players.hasPermission(VelocityConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                        continue;
                    }

                    if (VelocityConfig.STAFFLIST_BYPASS.get(Boolean.class) && players.hasPermission(VelocityConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                        continue;
                    }

                    list.add(players.getUniqueId());

                }
            }

            sb.append((VelocityMessages.DISCORDLIST_HEADER.get(String.class) + "\n")
                    .replace("%online%", String.valueOf(list.size())));

            if (list.isEmpty()) {
                sb.append(VelocityMessages.DISCORDLIST_NOBODY.get(String.class)).append("\n");
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

            for (UUID uuids : list) {

                Player players = PLUGIN.getServer().getPlayer(uuids).orElse(null);

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
                    isAFK = VelocityMessages.STAFFLIST_AFK.color();
                }

                if (group == null || group.getDisplayName() == null) {

                    final String prefix = user.getCachedData().getMetaData().getPrimaryGroup();

                    if (prefix != null) {
                        user_prefix = prefix;
                    } else {
                        user_prefix = "";
                    }

                    if (!players.getCurrentServer().isPresent()) {
                        continue;
                    }

                    sb.append((VelocityMessages.DISCORDLIST_FORMAT.get(String.class) + "\n")
                            .replace("%usergroup%", ChatUtil.translateHex(user_prefix))
                            .replace("%player%", players.getUsername())
                            .replace("%afk%", isAFK)
                            .replace("%server%", players.getCurrentServer().get().getServerInfo().getName()));

                    continue;
                }

                final String prefix = group.getDisplayName();
                user_prefix = prefix == null ? group.getDisplayName() : prefix;

                if (!players.getCurrentServer().isPresent()) {
                    continue;
                }

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
                embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);
                event.getChannel().sendMessageEmbeds(embed.build()).queue();

            } else {
                event.getMessage().reply(sb.toString()).queue();
            }

            return;
        }

        if (!event.getChannel().getId().equalsIgnoreCase(VelocityDiscordConfig.STAFF_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(VelocityMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        if (event.getAuthor().isBot() && !VelocityDiscordConfig.FORWARD_BOT.get(Boolean.class)) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {

            event.getMessage().reply(VelocityMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
            final String final_message = VelocityMessages.DISCORD_STAFF_FORMAT.get(String.class)
                    .replace("%user%", event.getAuthor().getName())
                    .replace("%message%", event.getMessage().getContentDisplay())
                    .replace("%prefix%", VelocityMessages.PREFIX.color())
                    .replace("&", "§");

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffMessage-RedisBungee", final_message);

        } else {

            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                    && !instance.isInBlockedStaffChatServer(players))
                    .forEach(players -> VelocityMessages.DISCORD_STAFF_FORMAT.send(players,
                            new Placeholder("user", event.getAuthor().getName()),
                            new Placeholder("message", event.getMessage().getContentDisplay()),
                            new Placeholder("prefix", VelocityMessages.PREFIX.color())));

        }
    }
}