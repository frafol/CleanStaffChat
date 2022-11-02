package it.frafol.cleanstaffchat.velocity.staffchat.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.UpdateCheck;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.awt.*;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.*;

public class JoinListener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void handle(PostLoginEvent event){

        if (event.getPlayer().hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))) {
            if (VelocityConfig.UPDATE_CHECK.get(Boolean.class)) {
                new UpdateCheck(PLUGIN).getVersion(version -> {
                    if (PLUGIN.container.getDescription().getVersion().isPresent()) {
                        if (!PLUGIN.container.getDescription().getVersion().get().equals(version)) {
                            event.getPlayer().sendMessage(Component.text("§e[CleanStaffChat] New update is available! Download it on https://bit.ly/3BOQFEz"));
                            PLUGIN.getLogger().warning("There is a new update available, download it on SpigotMC!");
                        }
                    }
                });
            }
        }

        if (!(CleanStaffChat.getInstance().getServer().getAllPlayers().size() < 1)) {
            Player player = event.getPlayer();
            if (STAFF_JOIN_MESSAGE.get(Boolean.class)) {
                if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                        || STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class)) {
                    if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());

                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;
                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFF_JOIN_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("displayname", user_prefix + player.getUsername() + user_suffix),
                                        new Placeholder("userprefix", user_prefix),
                                        new Placeholder("usersuffix", user_suffix),
                                        new Placeholder("prefix", PREFIX.color())));
                    } else {
                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFF_JOIN_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", PREFIX.color())));
                    }

                    if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class) && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) {

                        final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.STAFF_CHANNEL_ID.get(String.class));

                        assert channel != null;

                        if (VelocityConfig.USE_EMBED.get(Boolean.class)) {

                            EmbedBuilder embed = new EmbedBuilder();

                            embed.setTitle(VelocityConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                            embed.setDescription(VelocityConfig.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getUsername()));

                            embed.setColor(Color.YELLOW);
                            embed.setFooter("Powered by CleanStaffChat");

                            channel.sendMessageEmbeds(embed.build()).queue();

                        } else {

                            channel.sendMessageFormat(VelocityConfig.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                                    .replace("%user%", player.getUsername()))
                                    .queue();

                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void handle(DisconnectEvent event) {

        Player player = event.getPlayer();

        if (STAFF_QUIT_MESSAGE.get(Boolean.class)) {

            if (player.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                    || STAFFCHAT_QUIT_ALL.get(Boolean.class)) {

                if (PLUGIN.getServer().getPluginManager().isLoaded("luckperms")) {

                    LuckPerms api = LuckPermsProvider.get();

                    if (api.getUserManager().getUser(event.getPlayer().getUniqueId()) == null) {
                        return;
                    }

                    User user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
                    assert user != null;

                    final String prefix = user.getCachedData().getMetaData().getPrefix();
                    final String suffix = user.getCachedData().getMetaData().getSuffix();

                    final String user_prefix = prefix == null ? "" : prefix;
                    final String user_suffix = suffix == null ? "" : suffix;

                    if (CleanStaffChat.getInstance().getServer().getAllPlayers().size() >= 1) {

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFF_QUIT_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("displayname", user_prefix + player.getUsername() + user_suffix),
                                        new Placeholder("userprefix", user_prefix),
                                        new Placeholder("usersuffix", user_suffix),
                                        new Placeholder("prefix", PREFIX.color())));

                    }

                } else {

                    if (CleanStaffChat.getInstance().getServer().getAllPlayers().size() >= 1) {

                        CleanStaffChat.getInstance().getServer().getAllPlayers().stream().filter
                                        (players -> players.hasPermission(VelocityConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                                .forEach(players -> STAFF_QUIT_MESSAGE_FORMAT.send(players,
                                        new Placeholder("user", player.getUsername()),
                                        new Placeholder("prefix", PREFIX.color())));

                    }

                }

                if (VelocityConfig.DISCORD_ENABLED.get(Boolean.class)
                        && VelocityConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)
                        && VelocityConfig.JOIN_LEAVE_DISCORD_MODULE.get(Boolean.class)) {

                    final TextChannel channel = PLUGIN.getJda().getTextChannelById(VelocityConfig.STAFF_CHANNEL_ID.get(String.class));

                    assert channel != null;

                    if (VelocityConfig.USE_EMBED.get(Boolean.class)) {

                        EmbedBuilder embed = new EmbedBuilder();

                        embed.setTitle(VelocityConfig.STAFFCHAT_EMBED_TITLE.get(String.class), null);

                        embed.setDescription(VelocityConfig.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                                .replace("%user%", player.getUsername()));

                        embed.setColor(Color.YELLOW);
                        embed.setFooter("Powered by CleanStaffChat");

                        channel.sendMessageEmbeds(embed.build()).queue();

                    } else {

                        channel.sendMessageFormat(VelocityConfig.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class)
                                        .replace("%user%", player.getUsername()))
                                .queue();

                    }
                }
            }
        }

        PlayerCache.getAfk().remove(event.getPlayer().getUniqueId());

    }
}
