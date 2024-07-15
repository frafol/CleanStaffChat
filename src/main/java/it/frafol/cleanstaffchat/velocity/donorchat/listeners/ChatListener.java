package it.frafol.cleanstaffchat.velocity.donorchat.listeners;

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

        if (!PlayerCache.getToggled_2_donor().contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (PlayerCache.getCooldown().contains(event.getPlayer().getUniqueId())) {
            VelocityMessages.DONORCHAT_COOLDOWN_MESSAGE.send(event.getPlayer(), new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
            if (!VelocityConfig.DOUBLE_MESSAGE.get(Boolean.class)) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
            }
            return;
        }

        if (!event.getPlayer().hasPermission(DONORCHAT_USE_PERMISSION.get(String.class))) {
            ChatUtil.sendChannelMessage(event.getPlayer(), false);
            PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());
            return;
        }

        if (!(DONORCHAT_TALK_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(event.getPlayer(), new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
            return;
        }

        if (event.getMessage().startsWith("/")) {
            return;
        }

        if (PlayerCache.getMuted_donor().contains("true")) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            VelocityMessages.DONORCHAT_MUTED_ERROR.send(event.getPlayer(),
                    new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
            return;
        }

        if (PREVENT_COLOR_CODES.get(Boolean.class)) {
            if (ChatUtil.hasColorCodes(message)) {
                VelocityMessages.COLOR_CODES.send(event.getPlayer(),
                        new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
                return;
            }
        }

        if (!(event.getPlayer().getCurrentServer().isPresent())) {
            return;
        }

        if (VelocityServers.DONORCHAT_ENABLE.get(Boolean.class)) {
            for (String server : VelocityServers.DC_BLOCKED_SRV.getStringList()) {
                if (event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName().equalsIgnoreCase(server)) {
                    PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    ChatUtil.sendChannelMessage(event.getPlayer(), false);
                    VelocityMessages.DONORCHAT_MUTED_ERROR.send(event.getPlayer(), new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
                    return;
                }
            }
        }

        if (!event.getPlayer().hasPermission(COOLDOWN_BYPASS_PERMISSION.get(String.class))) {
            PlayerCache.getCooldown().add(event.getPlayer().getUniqueId());
            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> PlayerCache.getCooldown().remove(event.getPlayer().getUniqueId()))
                    .delay(DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
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
                final String final_message = VelocityMessages.DONORCHAT_FORMAT.get(String.class)
                        .replace("%user%", sender)
                        .replace("%message%", message)
                        .replace("%displayname%", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix))
                        .replace("%userprefix%", ChatUtil.translateHex(user_prefix))
                        .replace("%usersuffix%", ChatUtil.translateHex(user_suffix))
                        .replace("%server%", event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName())
                        .replace("%prefix%", VelocityMessages.PREFIX.color())
                        .replace("&", "§");

                redisBungeeAPI.sendChannelMessage("CleanStaffChat-DonorMessage-RedisBungee", final_message);
                return;

            }

            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled_donor().contains(players.getUniqueId()))
                                    && !instance.isInBlockedDonorChatServer(players))
                    .forEach(players -> VelocityMessages.DONORCHAT_FORMAT.send(players,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("displayname", ChatUtil.translateHex(user_prefix) + sender + ChatUtil.translateHex(user_suffix)),
                            new Placeholder("userprefix", ChatUtil.translateHex(user_prefix)),
                            new Placeholder("usersuffix", ChatUtil.translateHex(user_suffix)),
                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                            new Placeholder("prefix", VelocityMessages.DONORPREFIX.color())));

        } else {

            if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

                final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
                final String final_message = VelocityMessages.DONORCHAT_FORMAT.get(String.class)
                        .replace("%user%", sender)
                        .replace("%message%", message)
                        .replace("%displayname%", "")
                        .replace("%userprefix%", "")
                        .replace("%usersuffix%", "")
                        .replace("%server%", event.getPlayer().getCurrentServer().get().getServer().getServerInfo().getName())
                        .replace("%prefix%", VelocityMessages.DONORPREFIX.color())
                        .replace("&", "§");

                redisBungeeAPI.sendChannelMessage("CleanStaffChat-DonorMessage-RedisBungee", final_message);
                return;

            }

            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled_donor().contains(players.getUniqueId()))
                                    && !instance.isInBlockedDonorChatServer(players))
                    .forEach(players -> VelocityMessages.DONORCHAT_FORMAT.send(players,
                            new Placeholder("user", sender),
                            new Placeholder("message", message),
                            new Placeholder("displayname", sender),
                            new Placeholder("userprefix", ""),
                            new Placeholder("usersuffix", ""),
                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                            new Placeholder("prefix", VelocityMessages.DONORPREFIX.color())));

        }

        if (!VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {
            return;
        }

        final TextChannel channel = PLUGIN.getJda().JdaWorker().getTextChannelById(VelocityDiscordConfig.DONOR_CHANNEL_ID.get(String.class));

        if (channel == null) {
            return;
        }

        if (VelocityDiscordConfig.USE_EMBED.get(Boolean.class)) {

            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle(VelocityDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);

            embed.setDescription(VelocityMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                    .replace("%user%", sender)
                    .replace("%message%", message)
                    .replace("%server%", event.getPlayer().getCurrentServer().get().getServerInfo().getName()));

            embed.setColor(Color.getColor(VelocityDiscordConfig.EMBEDS_DONORCHATCOLOR.get(String.class)));
            embed.setFooter(VelocityDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

            channel.sendMessageEmbeds(embed.build()).queue();

        } else {

            channel.sendMessageFormat(VelocityMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
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

        if (!event.getChannel().getId().equalsIgnoreCase(VelocityDiscordConfig.DONOR_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(VelocityMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class))
                || event.getMessage().getContentDisplay().equalsIgnoreCase(VelocityMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        if (event.getAuthor().isBot() && !VelocityDiscordConfig.FORWARD_BOT.get(Boolean.class)) {
            return;
        }

        if (PlayerCache.getMuted_donor().contains("true")) {

            event.getMessage().reply(VelocityMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();
            return;

        }

        if (PlayerCache.getCooldown_discord().contains(event.getAuthor().getId())
                && (!VelocityConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class))) {

            event.getMessage().reply(VelocityMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class)).queue();

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();
            return;
        }

        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

            final String final_message = VelocityMessages.DISCORD_DONOR_FORMAT.get(String.class)
                    .replace("%user%", event.getAuthor().getName())
                    .replace("%message%", event.getMessage().getContentDisplay())
                    .replace("%prefix%", VelocityMessages.DONORPREFIX.color())
                    .replace("&", "§");

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-DonorMessage-RedisBungee", final_message);

        } else {

            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                            (players -> players.hasPermission(VelocityConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId()))
                                    && !instance.isInBlockedDonorChatServer(players))
                    .forEach(players -> VelocityMessages.DISCORD_DONOR_FORMAT.send(players,
                            new Placeholder("user", event.getAuthor().getName()),
                            new Placeholder("message", event.getMessage().getContentDisplay()),
                            new Placeholder("prefix", VelocityMessages.DONORPREFIX.color())));

        }

        if (!VelocityConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class)) {

            PlayerCache.getCooldown_discord().add(event.getAuthor().getId());

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> PlayerCache.getCooldown_discord().remove(event.getAuthor().getId()))
                    .delay(DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();

        }
    }
}