package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

public class MoveListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public MoveListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {

        final Player player = event.getPlayer();

        if (!PlayerCache.getAfk().contains(player.getUniqueId())) {
            return;
        }

        if (!player.hasPermission(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {
            return;
        }

        if (!SpigotConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class)) {
            return;
        }

        if (PlayerCache.getAfk().contains(player.getUniqueId())) {

            if (PLUGIN.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();
                final User user = api.getUserManager().getUser(player.getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", PlayerCache.color(user_prefix) + player.getName() + PlayerCache.color(user_suffix))
                                .replace("%userprefix%", PlayerCache.color(user_prefix))
                                .replace("%usersuffix%", PlayerCache.color(user_suffix))));

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

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", PlayerCache.color(ultraPermissionsUserPrefixFinal) + player.getName() + PlayerCache.color(ultraPermissionsUserSuffixFinal))
                                .replace("%userprefix%", PlayerCache.color(ultraPermissionsUserPrefixFinal))
                                .replace("%usersuffix%", PlayerCache.color(ultraPermissionsUserSuffixFinal))));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color(event.getPlayer())
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", player.getName())));

            }

            PlayerCache.getAfk().remove(player.getUniqueId());
            if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_AFK_MODULE.get(Boolean.class)) {

                final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return;
                }

                if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(SpigotMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName()));

                    embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                    embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {
                    channel.sendMessageFormat(SpigotMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())).queue();
                }
            }
        }
    }
}
