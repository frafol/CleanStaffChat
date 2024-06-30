package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class AFKCommand extends CommandBase {

    public AFKCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        
        if (!SpigotConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {
            sender.sendMessage((SpigotMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        if (!inGameCheck(sender)) {
            return false;
        }
        
        Player player = (Player) sender;
        if (!player.hasPermission(SpigotConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {
            player.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        if (!PlayerCache.getAfk().contains(player.getUniqueId())) {
            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(player.getUniqueId());

                if (user == null) {
                            return false;
                        }
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_ON.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", PlayerCache.color(user_prefix) + player.getName() + PlayerCache.color(user_suffix))
                                .replace("%userprefix%", PlayerCache.color(user_prefix))
                                .replace("%server%", "")
                                .replace("%usersuffix%", PlayerCache.color(user_suffix))));

            } else if (Bukkit.getServer().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(((Player) sender).getUniqueId()).isPresent()) {
                    return false;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((Player) sender).getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_ON.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%server%", "")
                                .replace("&", "§")));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_ON.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%userprefix%", "")
                                .replace("%server%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", player.getName())));

            }

            PlayerCache.getAfk().add(player.getUniqueId());

            if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_AFK_MODULE.get(Boolean.class)) {

                final TextChannel channel = plugin.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return false;
                }

                if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(SpigotMessages.STAFF_DISCORD_AFK_ON_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName()));

                    embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                    embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {
                    channel.sendMessageFormat(SpigotMessages.STAFF_DISCORD_AFK_ON_MESSAGE_FORMAT.get(String.class)
                            .replace("%user%", player.getName())).queue();
                }
            }

        } else {

            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(player.getUniqueId());

                if (user == null) {
                    return false;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", PlayerCache.color(user_prefix) + player.getName() + PlayerCache.color(user_suffix))
                                .replace("%userprefix%", PlayerCache.color(user_prefix))
                                .replace("%server%", "")
                                .replace("%usersuffix%", PlayerCache.color(user_suffix))));

            } else if (Bukkit.getServer().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(((Player) sender).getUniqueId()).isPresent()) {
                    return false;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((Player) sender).getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_admin().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + player.getName() + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%server%", "")
                                .replace("&", "§")));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_AFK_OFF.color(player)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", player.getName())
                                .replace("%userprefix%", "")
                                .replace("%server%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", player.getName())));

            }

            PlayerCache.getAfk().remove(player.getUniqueId());
            if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                    && SpigotConfig.STAFFCHAT_DISCORD_AFK_MODULE.get(Boolean.class)) {

                final TextChannel channel = plugin.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return false;
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
        return false;
    }
}
