package it.frafol.cleanstaffchat.bukkit.adminchat.listeners;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

public class ChatListener extends ListenerAdapter implements Listener {

    public final CleanStaffChat PLUGIN;

    public ChatListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        if (!PlayerCache.getToggled_2_admin().contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {
            PlayerCache.getToggled_2_admin().remove(event.getPlayer().getUniqueId());
            event.setCancelled(true);
            event.getPlayer().sendMessage(SpigotMessages.ADMINCHAT_MUTED_ERROR.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color()));
            return;
        }

        if (event.getMessage().startsWith("/")) {
            return;
        }

        if (!(SpigotConfig.ADMINCHAT_TALK_MODULE.get(Boolean.class))) {
            event.getPlayer().sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())
                    .replace("&", "§")));
        }

        if (event.getPlayer().hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))) {

            final String message = event.getMessage();
            event.setCancelled(true);

            if (SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {

                if (PlayerCache.hasColorCodes(message)) {
                    event.getPlayer().sendMessage(SpigotMessages.COLOR_CODES.color()
                            .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())
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
                                (players -> players.hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.ADMINCHAT_FORMAT.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())
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
                                (players -> players.hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.ADMINCHAT_FORMAT.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", event.getMessage())
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + event.getPlayer().getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%server%", "")
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("&", "§")));

            } else {

                PLUGIN.getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.ADMINCHAT_FORMAT.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", event.getMessage())
                                .replace("%server%", "")
                                .replace("&", "§")));

            }

            if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !SpigotConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return;
            }

            final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotDiscordConfig.ADMIN_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(SpigotDiscordConfig.ADMINCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(SpigotMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", event.getPlayer().getName())
                        .replace("%message%", message)
                        .replace("%server%", ""));

                embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_ADMINCHATCOLOR.get(String.class)));
                embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {

                channel.sendMessageFormat(SpigotMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", event.getPlayer().getName())
                                .replace("%message%", message)
                                .replace("%server%", ""))
                        .queue();

            }
            event.setCancelled(true);
        } else {
            PlayerCache.getToggled_2_admin().remove(event.getPlayer().getUniqueId());
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (PLUGIN.getConfigTextFile() == null) {
            return;
        }

        if (!event.getChannel().getId().equalsIgnoreCase(SpigotDiscordConfig.ADMIN_CHANNEL_ID.get(String.class))) {
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

        if (PlayerCache.getMuted_admin().contains("true")) {
            event.getMessage().reply(SpigotMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();
            TaskScheduler scheduler = UniversalScheduler.getScheduler(PLUGIN);
            scheduler.runTaskLaterAsynchronously(() -> event.getMessage().delete().queue(), 5L * 20L);
            return;
        }

        PLUGIN.getServer().getOnlinePlayers().stream().filter
                        (players -> players.hasPermission(SpigotConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                .forEach(players -> players.sendMessage(SpigotMessages.DISCORD_ADMIN_FORMAT.color()
                        .replace("%prefix%", SpigotMessages.ADMINPREFIX.color())
                        .replace("%user%", event.getAuthor().getName())
                        .replace("%message%", event.getMessage().getContentDisplay())));

    }
}