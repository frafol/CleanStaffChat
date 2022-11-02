package it.frafol.cleanstaffchat.bukkit.staffchat.listeners;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.UpdateCheck;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;

public class JoinListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {

        if (event.getPlayer().hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                && (SpigotConfig.UPDATE_CHECK.get(Boolean.class))) {
            new UpdateCheck(PLUGIN).getVersion(version -> {
                if (!PLUGIN.getDescription().getVersion().equals(version)) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz");
                    PLUGIN.getLogger().warning("There is a new update available, download it on SpigotMC!");
                }
            });
        }

        if (!(CleanStaffChat.getInstance().getServer().getOnlinePlayers().size() < 1)) {

            Player player = event.getPlayer();

            if (SpigotConfig.STAFF_JOIN_MESSAGE.get(Boolean.class)) {

                if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || SpigotConfig.STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {

                    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                        LuckPerms api = LuckPermsProvider.get();

                        User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                        assert user != null;

                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();

                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {

                            if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_JOIN_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                                .replace("%userprefix%", user_prefix)
                                                .replace("%usersuffix%", user_suffix)
                                                .replace("%user%", player.getName())));

                            }
                        }

                    } else {

                        for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {

                            if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_JOIN_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%user%", player.getName())));

                            }
                        }
                    }

                    if (SpigotConfig.DISCORD_ENABLED.get(Boolean.class)
                            && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                            && SpigotConfig.JOIN_LEAVE_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotConfig.STAFF_CHANNEL_ID.get(String.class));

                        assert channel != null;

                        if (SpigotConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(SpigotConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(SpigotConfig.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName()));

                            embed.setColor(Color.YELLOW);
                            embed.setFooter("Powered by CleanStaffChat");

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {

                            channel.sendMessageFormat(SpigotConfig.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getName())).queue();

                        }
                    }

                }
            }
        }
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (SpigotConfig.STAFF_QUIT_MESSAGE.get(Boolean.class)) {

            if (player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                    || SpigotConfig.STAFFCHAT_QUIT_ALL.get(Boolean.class)) {

                if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {

                    LuckPerms api = LuckPermsProvider.get();

                    User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                    assert user != null;

                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();

                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {

                        if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

                            if (!(CleanStaffChat.getInstance().getServer().getOnlinePlayers().size() < 1)) {
                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_QUIT_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%displayname%", user_prefix + player.getName() + user_suffix)
                                                .replace("%userprefix%", user_prefix)
                                                .replace("%usersuffix%", user_suffix)
                                                .replace("%user%", player.getName())));
                            }
                        }
                    }

                } else {
                    for (Player all : CleanStaffChat.getInstance().getServer().getOnlinePlayers()) {

                        if (all.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {

                            if (!(CleanStaffChat.getInstance().getServer().getOnlinePlayers().size() < 1)) {
                                CleanStaffChat.getInstance().getServer().getOnlinePlayers().stream().filter
                                                (players -> players.hasPermission
                                                        (SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)))
                                        .forEach(players -> players.sendMessage(SpigotConfig.STAFF_QUIT_MESSAGE_FORMAT.color()
                                                .replace("%prefix%", SpigotConfig.PREFIX.color())
                                                .replace("%user%", player.getName())));

                            }
                        }
                    }
                }

                if (SpigotConfig.DISCORD_ENABLED.get(Boolean.class)
                        && SpigotConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                        && SpigotConfig.JOIN_LEAVE_DISCORD_MODULE.get(Boolean.class)) {

                    final TextChannel channel = PLUGIN.getJda().getTextChannelById(SpigotConfig.STAFF_CHANNEL_ID.get(String.class));

                    assert channel != null;

                    if (SpigotConfig.USE_EMBED.get(Boolean.class)) {

                        EmbedBuilder embed = new EmbedBuilder();

                        embed.setTitle(SpigotConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                        embed.setDescription(SpigotConfig.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getName()));

                        embed.setColor(Color.YELLOW);
                        embed.setFooter("Powered by CleanStaffChat");

                        channel.sendMessageEmbeds(embed.build()).queue();

                    } else {

                        channel.sendMessageFormat(SpigotConfig.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getName())).queue();

                    }
                }

            }
        }

        PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

    }
}
