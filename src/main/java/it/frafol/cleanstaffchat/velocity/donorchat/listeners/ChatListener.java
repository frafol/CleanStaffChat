package it.frafol.cleanstaffchat.velocity.donorchat.listeners;

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

        if (PlayerCache.getToggled_2_donor().contains(event.getPlayer().getUniqueId())) {

            if (PlayerCache.getCooldown().contains(event.getPlayer().getUniqueId())) {

                DONORCHAT_COOLDOWN_MESSAGE.send(event.getPlayer(), new Placeholder("prefix", DONORPREFIX.color()));
                event.setResult(PlayerChatEvent.ChatResult.denied());

                return;

            }

            if (event.getPlayer().hasPermission(DONORCHAT_USE_PERMISSION.get(String.class))) {

                if (!(DONORCHAT_TALK_MODULE.get(Boolean.class))) {

                    MODULE_DISABLED.send(event.getPlayer(), new Placeholder("prefix", DONORPREFIX.color()));

                    return;

                }

                if (!event.getMessage().startsWith("/")) {

                    if (!PlayerCache.getMuted_donor().contains("true")) {

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
                                        new Placeholder("prefix", DONORPREFIX.color()));

                                return;
                            }
                        }

                        if (!(event.getPlayer().getCurrentServer().isPresent())) {

                            return;

                        }

                        if (!event.getPlayer().hasPermission(COOLDOWN_BYPASS_PERMISSION.get(String.class))) {

                            PlayerCache.getCooldown().add(event.getPlayer().getUniqueId());

                            PLUGIN.getServer().getScheduler()
                                    .buildTask(PLUGIN, scheduledTask -> PlayerCache.getCooldown().remove(event.getPlayer().getUniqueId()))
                                    .delay(DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS)
                                    .schedule();

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
                                            (players -> players.hasPermission(VelocityConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                    && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                    .forEach(players -> DONORCHAT_FORMAT.send(players,
                                            new Placeholder("user", sender),
                                            new Placeholder("message", message),
                                            new Placeholder("displayname", user_prefix + sender + user_suffix),
                                            new Placeholder("userprefix", user_prefix),
                                            new Placeholder("usersuffix", user_suffix),
                                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                                            new Placeholder("prefix", DONORPREFIX.color())));

                        } else {

                            CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                            (players -> players.hasPermission(VelocityConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                    && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                    .forEach(players -> DONORCHAT_FORMAT.send(players,
                                            new Placeholder("user", sender),
                                            new Placeholder("message", message),
                                            new Placeholder("displayname", sender),
                                            new Placeholder("userprefix", ""),
                                            new Placeholder("usersuffix", ""),
                                            new Placeholder("server", event.getPlayer().getCurrentServer().get().getServerInfo().getName()),
                                            new Placeholder("prefix", DONORPREFIX.color())));

                        }

                        if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class) && DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {

                            final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.DONOR_CHANNEL_ID.get(String.class));

                            assert channel != null;
                            channel.sendMessageFormat(VelocityConfig.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", sender)
                                            .replace("%message%", message)
                                            .replace("%server%", event.getPlayer().getCurrentServer().get().getServerInfo().getName()))
                                    .queue();

                        }

                    } else {

                        DONORCHAT_MUTED_ERROR.send(event.getPlayer(),
                                new Placeholder("prefix", DONORPREFIX.color()));

                    }
                }

            } else {

                PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());

            }
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {

            return;

        }

        if (!event.getChannel().getId().equalsIgnoreCase(VelocityConfig.DONOR_CHANNEL_ID.get(String.class))) {

            return;

        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class))
                || event.getMessage().getContentDisplay().equalsIgnoreCase(STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        if (event.getAuthor().isBot()) {

            return;

        }

        if (PlayerCache.getMuted_donor().contains("true")) {

            event.getMessage().reply(STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        if (PlayerCache.getCooldown_discord().contains(event.getAuthor().getId())
                && (!VelocityConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class))) {

            event.getMessage().reply(VelocityConfig.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class)).queue();

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> event.getMessage().delete().queue())
                    .delay(5, TimeUnit.SECONDS)
                    .schedule();

            return;

        }

        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                        (players -> players.hasPermission(VelocityConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                .forEach(players -> DISCORD_DONOR_FORMAT.send(players,
                        new Placeholder("user", event.getAuthor().getName()),
                        new Placeholder("message", event.getMessage().getContentDisplay()),
                        new Placeholder("prefix", DONORPREFIX.color())));

        if (!VelocityConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class)) {

            PlayerCache.getCooldown_discord().add(event.getAuthor().getId());

            PLUGIN.getServer().getScheduler()
                    .buildTask(PLUGIN, scheduledTask -> PlayerCache.getCooldown_discord().remove(event.getAuthor().getId()))
                    .delay(DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();

        }
    }
}