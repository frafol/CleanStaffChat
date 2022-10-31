package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class ChatListener extends ListenerAdapter {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChat(PlayerChatEvent event) {

        final String message = event.getMessage();
        final String sender = event.getPlayer().getUsername();

        if (PlayerCache.getToggled_2().contains(event.getPlayer().getUniqueId())) {

            if (event.getPlayer().hasPermission(STAFFCHAT_USE_PERMISSION.get(String.class))) {

                if (!(STAFFCHAT_TALK_MODULE.get(Boolean.class))) {

                    MODULE_DISABLED.send(event.getPlayer(), new Placeholder("prefix", PREFIX.color()));

                    return;

                }

                if (!event.getMessage().startsWith("/")) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        if (PREVENT_COLOR_CODES.get(Boolean.class)) {
                            if (message.contains("&0") ||
                                    message.contains("&1") ||
                                    message.contains("&2") ||
                                    message.contains("&3") ||
                                    message.contains("&4") ||
                                    message.contains("&5") ||
                                    message.contains("&6") ||
                                    message.contains("&7") ||
                                    message.contains("&8") ||
                                    message.contains("&9") ||
                                    message.contains("&a") ||
                                    message.contains("&b") ||
                                    message.contains("&c") ||
                                    message.contains("&d") ||
                                    message.contains("&e") ||
                                    message.contains("&f") ||
                                    message.contains("&k") ||
                                    message.contains("&l") ||
                                    message.contains("&m") ||
                                    message.contains("&n") ||
                                    message.contains("&o") ||
                                    message.contains("&r")) {

                                COLOR_CODES.send(event.getPlayer(),
                                        new Placeholder("prefix", PREFIX.color()));

                                return;
                            }
                        }

                        if (!(event.getPlayer().getCurrentServer().isPresent())) {

                            return;

                        }

                        if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                            LuckPerms api = LuckPermsProvider.get();
                            event.setResult(PlayerChatEvent.ChatResult.denied());

                            User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                            assert user != null;
                            final String prefix = user.getCachedData().getMetaData().getPrefix();
                            final String suffix = user.getCachedData().getMetaData().getSuffix();
                            final String user_prefix = prefix == null ? "" : prefix;
                            final String user_suffix = suffix == null ? "" : suffix;

                            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                            (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                    .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                            new Placeholder("user", sender),
                                            new Placeholder("message", message),
                                            new Placeholder("displayname", user_prefix + sender + user_suffix),
                                            new Placeholder("userprefix", user_prefix),
                                            new Placeholder("usersuffix", user_suffix),
                                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                                            new Placeholder("prefix", PREFIX.color())));

                        } else {

                            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                            (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                    .forEach(players -> STAFFCHAT_FORMAT.send(players,
                                            new Placeholder("user", sender),
                                            new Placeholder("message", message),
                                            new Placeholder("displayname", sender),
                                            new Placeholder("userprefix", ""),
                                            new Placeholder("usersuffix", ""),
                                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                                            new Placeholder("prefix", PREFIX.color())));

                        }

                        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                            final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.STAFF_CHANNEL_ID.get(String.class));

                            assert channel != null;
                            channel.sendMessageFormat(VelocityConfig.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", sender)
                                            .replace("%message%", message)
                                            .replace("%server%", event.getPlayer().getCurrentServer().get().getServerInfo().getName()))
                                    .queue();

                        }

                    } else {

                        STAFFCHAT_MUTED_ERROR.send(event.getPlayer(),
                                new Placeholder("prefix", PREFIX.color()));

                    }
                }

            } else {

                PlayerCache.getToggled_2().remove(event.getPlayer().getUniqueId());

            }
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {

            return;

        }

        if (!event.getChannel().getId().equalsIgnoreCase(VelocityConfig.STAFF_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        if (event.getAuthor().isBot()) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {

            event.getMessage().reply(STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                .forEach(players -> DISCORD_STAFF_FORMAT.send(players,
                        new Placeholder("user", event.getAuthor().getName()),
                        new Placeholder("message", event.getMessage().getContentDisplay()),
                        new Placeholder("prefix", PREFIX.color())));

    }
}