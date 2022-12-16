package it.frafol.cleanstaffchat.bukkit.donorchat.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

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

                sender.sendMessage(("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));

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

                    if (SpigotConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
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

                            sender.sendMessage(SpigotMessages.COLOR_CODES.color()
                                    .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                    .replace("&", "§"));

                            return false;

                        }
                    }

                    if (PlayerCache.getCooldown().contains(((Player) sender).getUniqueId())) {

                        sender.sendMessage(SpigotMessages.DONORCHAT_COOLDOWN_MESSAGE.color()
                                .replace("%prefix%", SpigotMessages.DONORPREFIX.color()));

                        return false;

                    }

                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(((Player) sender).getUniqueId());
                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%displayname%", user_prefix + commandsender + user_suffix)
                                        .replace("%message%", message)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("%server%", "")
                                        .replace("&", "§")));

                    } else {

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(SpigotMessages.DONORCHAT_FORMAT.color()
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

                        assert channel != null;

                        if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(SpigotDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", commandsender)
                                    .replace("%message%", message)
                                    .replace("%server%", ""));

                            embed.setColor(Color.RED);
                            embed.setFooter("Powered by CleanStaffChat");

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

                        new BukkitRunnable() {
                            @Override
                            public void run() {

                                PlayerCache.getCooldown().remove(((Player) sender).getUniqueId());
                                cancel();

                            }

                        }.runTaskTimer(plugin, Math.multiplyExact(SpigotConfig.DONOR_TIMER.get(Integer.class), 20), 1);

                    }

                } else if (SpigotConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {

                    if (!PlayerCache.getMuted().contains("true")) {

                        CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                        (players -> players.hasPermission(SpigotConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage((SpigotMessages.DONORCHAT_FORMAT.color()
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

                    sender.sendMessage((SpigotMessages.DONORCHAT_FORMAT.color()
                            .replace("%prefix%", SpigotMessages.DONORPREFIX.color())
                            .replace("%user%", commandsender)
                            .replace("%displayname%", commandsender)
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", "")
                            .replace("%message%", message)));

                    if (SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && SpigotConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = plugin.getJda().getTextChannelById(SpigotDiscordConfig.DONOR_CHANNEL_ID.get(String.class));

                        assert channel != null;

                        if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(SpigotDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(SpigotMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", commandsender)
                                    .replace("%message%", message)
                                    .replace("%server%", ""));

                            embed.setColor(Color.RED);
                            embed.setFooter("Powered by CleanStaffChat");

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