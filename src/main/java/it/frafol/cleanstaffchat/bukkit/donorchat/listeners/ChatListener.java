package it.frafol.cleanstaffchat.bukkit.donorchat.listeners;

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
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        if (!PlayerCache.getToggled_2_donor().contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {

            PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());

            event.setCancelled(true);

            event.getPlayer().sendMessage(SpigotMessages.DONORCHAT_MUTED_ERROR.color()
                    .replace("%prefix%", SpigotMessages.DONORPREFIX.color()));

            return;

        }

        if (PlayerCache.getCooldown().contains(event.getPlayer().getUniqueId())) {

            PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());

            event.setCancelled(true);

            event.getPlayer().sendMessage(SpigotMessages.DONORCHAT_COOLDOWN_MESSAGE.color()
                    .replace("%prefix%", SpigotMessages.DONORPREFIX.color()));

            return;

        }

        if (event.getMessage().startsWith("/")) {
            return;
        }

        if (!(SpigotConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {

            event.getPlayer().sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                    .replace("&", "§")));

        } else if (event.getPlayer().hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {

            final String message = event.getMessage();

            if (SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {

                if (PlayerCache.hasColorCodes(message)) {

                    event.getPlayer().sendMessage(SpigotMessages.COLOR_CODES.color()
                            .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                            .replace("&", "§"));

                    event.setCancelled(true);

                    return;

                }
            }

            if (event.getPlayer().hasPermission(SpigotConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) {

                PlayerCache.getCooldown().add(event.getPlayer().getUniqueId());

                new BukkitRunnable() {
                    @Override
                    public void run() {

                        PlayerCache.getCooldown().remove(event.getPlayer().getUniqueId());
                        cancel();

                    }

                }.runTaskTimer(PLUGIN, Math.multiplyExact(SpigotConfig.DONOR_TIMER.get(Integer.class), 20), 1);

            }

            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                if (user == null) {
                    return;
                }
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", event.getMessage())
                                .replace("%displayname%", PlayerCache.translateHex(user_prefix) + event.getPlayer().getName() + PlayerCache.translateHex(user_suffix))
                                .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                .replace("%server%", "")
                                .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                                .replace("&", "§")));

            } else if (Bukkit.getServer().getPluginManager().getPlugin("UltraPermissions") != null) {

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

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + event.getPlayer().getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%message%", message)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%server%", "")
                                .replace("&", "§")));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", event.getMessage())
                                .replace("%server%", "")
                                .replace("&", "§")));

            }

            if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !SpigotConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return;
            }

            final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotDiscordConfig.DONOR_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(SpigotDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", event.getPlayer().getName())
                        .replace("%message%", message)
                        .replace("%server%", ""));

                embed.setColor(Color.RED);
                embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {

                channel.sendMessageFormat(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", message)
                                .replace("%server%", ""))
                        .queue();

            }
            event.setCancelled(true);
        } else {
            PlayerCache.getToggled_2_donor().remove(event.getPlayer().getUniqueId());
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {
            return;
        }

        if (!event.getChannel().getId().equalsIgnoreCase(SpigotDiscordConfig.DONOR_CHANNEL_ID.get(String.class))) {
            return;
        }

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(SpigotMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class))
                || event.getMessage().getContentDisplay().equalsIgnoreCase(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {

            ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(1);
            service.schedule(() -> event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);
            service.shutdown();

            return;

        }

        if (event.getAuthor().isBot()) {

            return;

        }

        if (PlayerCache.getMuted_donor().contains("true")) {

            event.getMessage().reply(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();

            ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(1);
            service.schedule(() -> event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);
            service.shutdown();

            return;

        }

        if (PlayerCache.getCooldown_discord().contains(event.getAuthor().getId())
                && (!SpigotConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class))) {

            event.getMessage().reply(SpigotMessages.DONORCHAT_COOLDOWN_ERROR_DISCORD.get(String.class)).queue();

            ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(1);
            service.schedule(() -> event.getMessage().delete().queue(), 5, TimeUnit.SECONDS);
            service.shutdown();

            return;

        }

        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                .forEach(players -> players.sendMessage((SpigotMessages.DISCORD_DONOR_FORMAT.color()
                        .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                        .replace("%user%", event.getAuthor().getName())
                        .replace("%message%", event.getMessage().getContentDisplay())
                        .replace("&", "§"))));

        if (!SpigotConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class)) {

            PlayerCache.getCooldown_discord().add(event.getAuthor().getId());

            new BukkitRunnable() {
                @Override
                public void run() {

                    PlayerCache.getCooldown_discord().remove(event.getAuthor().getId());
                    cancel();

                }

            }.runTaskTimer(PLUGIN, Math.multiplyExact(SpigotConfig.DONOR_TIMER.get(Integer.class), 20), 1);

        }
    }
}