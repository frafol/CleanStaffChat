package it.frafol.cleanstaffchat.velocity.adminchat.listeners;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
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
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
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

        if (!PlayerCache.getToggled_2_admin().contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (!event.getPlayer().hasPermission(ADMINCHAT_USE_PERMISSION.get(String.class))) {
            ChatUtil.sendChannelMessage(event.getPlayer(), false);
            PlayerCache.getToggled_2_admin().remove(event.getPlayer().getUniqueId());
            return;
        }

        if (!(ADMINCHAT_TALK_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(event.getPlayer(), new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        if (event.getMessage().startsWith("/")) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            VelocityMessages.ADMINCHAT_MUTED_ERROR.send(event.getPlayer(),
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            return;
        }

        if (PREVENT_COLOR_CODES.get(Boolean.class)) {
            if (ChatUtil.hasColorCodes(message)) {
                VelocityMessages.COLOR_CODES.send(event.getPlayer(),
                        new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
                return;
            }
        }

        if (!(event.getPlayer().getCurrentServer().isPresent())) {
            return;
        }

        if (VelocityServers.ADMINCHAT_ENABLE.get(Boolean.class)) {
            for (String server : VelocityServers.AC_BLOCKED_SRV.getStringList()) {
                if (event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName().equalsIgnoreCase(server)) {
                    PlayerCache.getToggled_2_admin().remove(event.getPlayer().getUniqueId());
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    ChatUtil.sendChannelMessage(event.getPlayer(), false);
                    VelocityMessages.ADMINCHAT_MUTED_ERROR.send(event.getPlayer(), new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
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

            if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                final String final_message = VelocityMessages.ADMINCHAT_FORMAT.get(String.class)
                        .replace("%user%", sender)
                        .replace("%message%", message)
                        .replace("%displayname%", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix))
                        .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                        .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                        .replace("%server%", event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName())
                        .replace("%prefix%", VelocityMessages.ADMINPREFIX.color())
                        .replace("&", "§");

                redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);
                return;
            }

            PLUGIN.getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                    && !instance.isInBlockedAdminChatServer(players))
                    .forEach(players -> VelocityMessages.ADMINCHAT_FORMAT.send(players,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix)),
                            new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                            new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                            new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color())));

        } else {

            if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                final String final_message = VelocityMessages.ADMINCHAT_FORMAT.get(String.class)
                        .replace("%user%", sender)
                        .replace("%message%", message)
                        .replace("%displayname%", sender)
                        .replace("%userprefix%", "")
                        .replace("%usersuffix%", "")
                        .replace("%server%", event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName())
                        .replace("%prefix%", VelocityMessages.ADMINPREFIX.color())
                        .replace("&", "§");

                redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);
                return;
            }

            PLUGIN.getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled_admin().contains(players.getUniqueId()))
                                    && !instance.isInBlockedAdminChatServer(players))
                    .forEach(players -> VelocityMessages.ADMINCHAT_FORMAT.send(players,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("displayname", sender),
                            new Placeholder("userprefix", ""),
                            new Placeholder("usersuffix", ""),
                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                            new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color())));

        }

        if (!VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !VelocityConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {
            return;
        }

        final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.ADMIN_CHANNEL_ID.get(String.class));

        if (channel == null) {
            return;
        }

        if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(VelocityDiscordConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

            embed.setDescription(VelocityMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                    .replace("%user%", sender)
                    .replace("%message%", message)
                    .replace("%server%", event.getPlayer().getCurrentServer().get().getServerInfo().getName()));

            embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_ADMINCHATCOLOR.get(String.class)));
            embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

            channel.sendMessageEmbeds(embed.build()).queue();

        } else {

            channel.sendMessageFormat(VelocityMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                            .replace("%user%", sender)
                            .replace("%message%", message)
                            .replace("%server%", event.getPlayer().getCurrentServer().get().getServerInfo().getName()))
                    .queue();

        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null || PLUGIN.getMessagesTextFile() == null) {
            return;
        }

        if (!event.getChannel().getId().equalsIgnoreCase(VelocityDiscordConfig.ADMIN_CHANNEL_ID.get(String.class))) {
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

        if (PlayerCache.getMuted_admin().contains("true")) {

            event.getMessage().reply(VelocityMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();
            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();
            return;
        }

        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

            final String final_message = VelocityMessages.DISCORD_ADMIN_FORMAT.get(String.class)
                    .replace("%user%", event.getAuthor().getName())
                    .replace("%message%", event.getMessage().getContentDisplay())
                    .replace("%prefix%", VelocityMessages.ADMINPREFIX.color())
                    .replace("&", "§");

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-AdminMessage-RedisBungee", final_message);

        } else {

            PLUGIN.getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                    && !instance.isInBlockedAdminChatServer(players))
                    .forEach(players -> VelocityMessages.DISCORD_ADMIN_FORMAT.send(players,
                            new Placeholder("user", event.getAuthor().getName()),
                            new Placeholder("message", event.getMessage().getContentDisplay()),
                            new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color())));
        }
    }
}