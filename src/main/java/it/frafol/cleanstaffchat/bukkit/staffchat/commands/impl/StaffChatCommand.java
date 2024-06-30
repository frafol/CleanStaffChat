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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StaffChatCommand extends CommandBase {

    public StaffChatCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {

        if (args.length == 0) {

            if (!inGameCheck(sender, SpigotMessages.ARGUMENTS.color().replace("%prefix%", SpigotMessages.PREFIX.color()))) {
                return false;
            }

            Player player = (Player) sender;

            if (sender.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

                if (!PlayerCache.getToggled_2().contains(player.getUniqueId())) {

                    if (!(SpigotConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                        sender.sendMessage((SpigotMessages.ARGUMENTS.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())));
                        return false;
                    }

                    if (!PlayerCache.getMuted().contains("true")) {
                        PlayerCache.getToggled_2().add(player.getUniqueId());
                        PlayerCache.getToggled_2_donor().remove(player.getUniqueId());
                        PlayerCache.getToggled_2_admin().remove(player.getUniqueId());

                        sender.sendMessage((SpigotMessages.STAFFCHAT_TALK_ENABLED.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())));

                    } else {
                        sender.sendMessage((SpigotMessages.ARGUMENTS.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())));
                        return false;
                    }
                    return false;

                } else if (PlayerCache.getToggled_2().contains(player.getUniqueId())) {
                    PlayerCache.getToggled_2().remove(player.getUniqueId());
                    sender.sendMessage((SpigotMessages.STAFFCHAT_TALK_DISABLED.color()
                            .replace("%prefix%", SpigotMessages.PREFIX.color())));
                    return false;
                }

            } else {

                if (SpigotConfig.HIDE_ADVERTS.get(Boolean.class) != null && !SpigotConfig.HIDE_ADVERTS.get(Boolean.class)) {
                    sender.sendMessage(("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));
                }

                return false;

            }

            return false;

        }

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        String commandsender = !(sender instanceof Player) ? SpigotConfig.CONSOLE_PREFIX.get(String.class) :
                sender.getName();

        if (!sender.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
            sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        if (PlayerCache.getMuted().contains("true")) {
            sender.sendMessage((SpigotMessages.STAFFCHAT_MUTED_ERROR.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
            return false;
        }

        if (sender instanceof Player) {

            if (SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class) && PlayerCache.hasColorCodes(message)) {
                sender.sendMessage(SpigotMessages.COLOR_CODES.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())
                        .replace("&", "§"));
                return false;
            }

            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                LuckPerms api = LuckPermsProvider.get();

                User user = api.getUserManager().getUser(((Player) sender).getUniqueId());

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
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color((Player) sender)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%displayname%", PlayerCache.color(user_prefix) + commandsender + PlayerCache.color(user_suffix))
                                .replace("%message%", message)
                                .replace("%userprefix%", PlayerCache.color(user_prefix))
                                .replace("%usersuffix%", PlayerCache.color(user_suffix))
                                .replace("%server%", "")));

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
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color((Player) sender)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%displayname%", PlayerCache.color(ultraPermissionsUserPrefixFinal) + commandsender + PlayerCache.color(ultraPermissionsUserSuffixFinal))
                                .replace("%message%", message)
                                .replace("%userprefix%", PlayerCache.color(ultraPermissionsUserPrefixFinal))
                                .replace("%usersuffix%", PlayerCache.color(ultraPermissionsUserSuffixFinal))
                                .replace("%server%", "")));

            } else {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(SpigotMessages.STAFFCHAT_FORMAT.color((Player) sender)
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", commandsender)
                                .replace("%message%", message)
                                .replace("%server%", "")));

            }

            if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                final TextChannel channel = plugin.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return false;
                }

                if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                            .replace("%user%", commandsender)
                            .replace("%message%", message)
                            .replace("%server%", ""));

                    embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                    embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {

                    channel.sendMessageFormat(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", commandsender)
                                    .replace("%message%", message)
                                    .replace("%server%", ""))
                            .queue();

                }
            }

        } else if (SpigotConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {

            if (!PlayerCache.getMuted().contains("true")) {

                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                (players -> players.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage((SpigotMessages.STAFFCHAT_CONSOLE_FORMAT.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", commandsender)
                                .replace("%server%", "")
                                .replace("%message%", message))));

            } else {

                sender.sendMessage((SpigotMessages.STAFFCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())));

                return false;

            }

            sender.sendMessage((SpigotMessages.STAFFCHAT_CONSOLE_FORMAT.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())
                    .replace("%user%", commandsender)
                    .replace("%displayname%", commandsender)
                    .replace("%userprefix%", "")
                    .replace("%usersuffix%", "")
                    .replace("%server%", "")
                    .replace("%message%", message)));

            if (!SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return false;
            }

            final TextChannel channel = plugin.getJda().getTextChannelById(SpigotDiscordConfig.STAFF_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return false;
            }

            if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(SpigotDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", commandsender)
                        .replace("%message%", message)
                        .replace("%server%", ""));

                embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
                embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {

                channel.sendMessageFormat(SpigotMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", commandsender)
                                .replace("%message%", message)
                                .replace("%server%", ""))
                        .queue();

            }
            return false;
        } else {
            sender.sendMessage((SpigotMessages.PLAYER_ONLY.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
        }
        return false;
    }
}