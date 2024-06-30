package it.frafol.cleanstaffchat.bukkit.donorchat.commands;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
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

public class DonorChatCommand extends CommandBase {

    public DonorChatCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if (args.length == 0) {

            if (!inGameCheck(sender, SpigotMessages.DONORARGUMENTS.color().replace("%prefix%", SpigotMessages.PREFIX.color()))) {
                return false;
            }

            Player player = (Player) sender;

            if (sender.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {

                if (!PlayerCache.getToggled_2_donor().contains(player.getUniqueId())) {

                    if (!(SpigotConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {
                        sender.sendMessage((SpigotMessages.DONORARGUMENTS.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));
                        return false;
                    }

                    if (!PlayerCache.getMuted().contains("true")) {

                        PlayerCache.getToggled_2_donor().add(player.getUniqueId());
                        PlayerCache.getToggled_2_admin().remove(player.getUniqueId());
                        PlayerCache.getToggled_2().remove(player.getUniqueId());

                        sender.sendMessage((SpigotMessages.DONORCHAT_TALK_ENABLED.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));

                    } else {

                        sender.sendMessage((SpigotMessages.DONORARGUMENTS.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));

                        return false;

                    }

                    return false;

                } else if (PlayerCache.getToggled_2_donor().contains(player.getUniqueId())) {
                    PlayerCache.getToggled_2_donor().remove(player.getUniqueId());
                    sender.sendMessage((SpigotMessages.DONORCHAT_TALK_DISABLED.color()
                            .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));

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

        if (sender.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {

            if (!PlayerCache.getMuted().contains("true")) {

                if (sender instanceof Player) {

                    if (!SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
                        return false;
                    }

                    if (PlayerCache.hasColorCodes(message)) {
                        sender.sendMessage(SpigotMessages.COLOR_CODES.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                .replace("&", "§"));
                        return false;
                    }


                    if (PlayerCache.getCooldown().contains(((Player) sender).getUniqueId())) {
                        sender.sendMessage(SpigotMessages.DONORCHAT_COOLDOWN_MESSAGE.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color()));
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
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color((Player) sender)
                                        .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%displayname%", PlayerCache.color(user_prefix) + commandsender + PlayerCache.color(user_suffix))
                                        .replace("%message%", message)
                                        .replace("%userprefix%", PlayerCache.color(user_prefix))
                                        .replace("%usersuffix%", PlayerCache.color(user_suffix))
                                        .replace("%server%", "")
                                        .replace("&", "§")));

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
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color((Player) sender)
                                        .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%displayname%", ultraPermissionsUserPrefixFinal + commandsender + ultraPermissionsUserSuffixFinal)
                                        .replace("%message%", message)
                                        .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                        .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                        .replace("%server%", "")
                                        .replace("&", "§")));

                    } else {

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color((Player) sender)
                                        .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%displayname%", commandsender)
                                        .replace("%message%", message)
                                        .replace("%server%", "")
                                        .replace("&", "§")));

                    }

                    if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && SpigotConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = plugin.getJda().getTextChannelById(SpigotDiscordConfig.DONOR_CHANNEL_ID.get(String.class));

                        if (channel == null) {
                            return false;
                        }

                        if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle(SpigotDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);
                            embed.setDescription(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", commandsender)
                                    .replace("%message%", message)
                                    .replace("%server%", ""));

                            embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_DONORCHATCOLOR.get(String.class)));
                            embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {
                            channel.sendMessageFormat(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", commandsender)
                                            .replace("%message%", message)
                                            .replace("%server%", ""))
                                    .queue();
                        }
                    }

                    if (!sender.hasPermission(SpigotConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) {
                        PlayerCache.getCooldown().add(((Player) sender).getUniqueId());
                        TaskScheduler scheduler = UniversalScheduler.getScheduler(plugin);
                        scheduler.runTaskLaterAsynchronously(() ->
                                PlayerCache.getCooldown().remove(((Player) sender).getUniqueId()), SpigotConfig.DONOR_TIMER.get(Integer.class) * 20L);
                    }

                } else if (SpigotConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage((SpigotMessages.DONORCHAT_CONSOLE_FORMAT.color()
                                        .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%displayname%", commandsender)
                                        .replace("%server%", "")
                                        .replace("%message%", message))));

                    } else {
                        sender.sendMessage((SpigotMessages.DONORCHAT_MUTED_ERROR.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));
                        return false;
                    }

                    sender.sendMessage((SpigotMessages.DONORCHAT_CONSOLE_FORMAT.color()
                            .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                            .replace("%user%", commandsender)
                            .replace("%displayname%", commandsender)
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", "")
                            .replace("%message%", message)));

                    if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && SpigotConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = plugin.getJda().getTextChannelById(SpigotDiscordConfig.DONOR_CHANNEL_ID.get(String.class));

                        if (channel == null) {
                            return false;
                        }

                        if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(SpigotDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", commandsender)
                                    .replace("%message%", message)
                                    .replace("%server%", ""));

                            embed.setColor(Color.getColor(SpigotDiscordConfig.EMBEDS_DONORCHATCOLOR.get(String.class)));
                            embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {
                            channel.sendMessageFormat(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                            .replace("%user%", commandsender)
                                            .replace("%message%", message)
                                            .replace("%server%", ""))
                                    .queue();
                        }
                    }

                    return false;

                } else {
                    sender.sendMessage((SpigotMessages.PLAYER_ONLY.color()
                            .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));
                }

                return false;
            } else {
                sender.sendMessage((SpigotMessages.DONORCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));
            }

            return false;
        } else {
            sender.sendMessage((SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.DONORPREFIX.color())));
        }

        return false;
    }
}