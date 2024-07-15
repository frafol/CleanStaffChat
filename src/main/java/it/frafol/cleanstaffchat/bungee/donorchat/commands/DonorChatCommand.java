package it.frafol.cleanstaffchat.bungee.donorchat.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.*;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DonorChatCommand extends Command {

    public final CleanStaffChat plugin;

    public DonorChatCommand(CleanStaffChat plugin) {

        super(BungeeCommandsConfig.DONORCHAT.getStringList().get(0),"", BungeeCommandsConfig.DONORCHAT.getStringList().toArray(new String[0]));

        this.plugin = plugin;

    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length == 0) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORARGUMENTS.color()
                        .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                return;
            }

            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (BungeeServers.DONORCHAT_ENABLE.get(Boolean.class)) {
                for (String server : BungeeServers.DC_BLOCKED_SRV.getStringList()) {

                    if (((ProxiedPlayer) sender).getServer() == null) {
                        return;
                    }

                    if (((ProxiedPlayer) sender).getServer().getInfo().getName().equalsIgnoreCase(server)) {
                        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_MUTED_ERROR.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                        return;
                    }
                }
            }

            if (sender.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {

                if (PlayerCache.getCooldown().contains(player.getUniqueId())) {
                    player.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_COOLDOWN_MESSAGE.color()
                            .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                    return;
                }

                if (!PlayerCache.getToggled_2_donor().contains(player.getUniqueId())) {
                    if (!(BungeeConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {
                        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORARGUMENTS.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                        return;
                    }
                    if (!PlayerCache.getMuted_donor().contains("true")) {
                        PlayerCache.getToggled_2_donor().add(player.getUniqueId());
                        PlayerCache.getToggled_2_admin().remove(player.getUniqueId());
                        PlayerCache.getToggled_2().remove(player.getUniqueId());
                        PlayerCache.sendChannelMessage(player, true);
                        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_TALK_ENABLED.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                        return;
                    } else {
                        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORARGUMENTS.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                    }
                } else if (PlayerCache.getToggled_2_donor().contains(player.getUniqueId())) {
                    PlayerCache.getToggled_2_donor().remove(player.getUniqueId());
                    PlayerCache.sendChannelMessage(player, false);
                    sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_TALK_DISABLED.color()
                            .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                    return;
                }

            } else {
                if (BungeeConfig.HIDE_ADVERTS.get(Boolean.class) != null && !BungeeConfig.HIDE_ADVERTS.get(Boolean.class)) {
                    sender.sendMessage(TextComponent.fromLegacy("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));
                }
            }
            return;
        }

        final String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        final String commandsender = !(sender instanceof ProxiedPlayer) ? BungeeConfig.CONSOLE_PREFIX.get(String.class) :
                sender.getName();

        if (!sender.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
            return;
        }

        if (PlayerCache.getMuted_donor().contains("true")) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_MUTED_ERROR.color()
                    .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
            return;
        }

        if (sender instanceof ProxiedPlayer) {

            if (BungeeConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
                if (PlayerCache.hasColorCodes(message)) {
                    sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.COLOR_CODES.color()
                            .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                            .replace("&", "§")));
                    return;
                }
            }

            if (BungeeServers.DONORCHAT_ENABLE.get(Boolean.class)) {
                for (String server : BungeeServers.DC_BLOCKED_SRV.getStringList()) {

                    if (((ProxiedPlayer) sender).getServer() == null) {
                        return;
                    }

                    if (((ProxiedPlayer) sender).getServer().getInfo().getName().equalsIgnoreCase(server)) {
                        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_MUTED_ERROR.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                        return;
                    }
                }
            }

            if (PlayerCache.getCooldown().contains(((ProxiedPlayer) sender).getUniqueId())) {
                sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_COOLDOWN_MESSAGE.color()
                        .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
                return;
            }

            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((ProxiedPlayer) sender).getUniqueId());

                if (user == null) {
                    return;
                }

                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.DONORCHAT_FORMAT.get(String.class)
                            .replace("%user%", commandsender)
                            .replace("%message%", message)
                            .replace("%displayname%", PlayerCache.translateHex(user_prefix) + commandsender + PlayerCache.translateHex(user_suffix))
                            .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                            .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-DonorMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_donor().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedDonorChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%message%", message)
                                .replace("%displayname%", PlayerCache.translateHex(user_prefix) + commandsender + PlayerCache.translateHex(user_suffix))
                                .replace("%userprefix%", PlayerCache.translateHex(user_prefix))
                                .replace("%usersuffix%", PlayerCache.translateHex(user_suffix))
                                .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                .replace("&", "§"))));
            } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {

                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();

                if (!userList.uuid(((ProxiedPlayer) sender).getUniqueId()).isPresent()) {
                    return;
                }

                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((ProxiedPlayer) sender).getUniqueId()).get();

                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                final String ultraPermissionsUserPrefixFinal = ultraPermissionsUserPrefix.orElse("");
                final String ultraPermissionsUserSuffixFinal = ultraPermissionsUserSuffix.orElse("");

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.DONORCHAT_FORMAT.get(String.class)
                            .replace("%user%", commandsender)
                            .replace("%message%", message)
                            .replace("%displayname%", ultraPermissionsUserPrefixFinal + commandsender + ultraPermissionsUserSuffixFinal)
                            .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                            .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-DonorMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_donor().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedDonorChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%message%", message)
                                .replace("%displayname%", ultraPermissionsUserPrefixFinal + commandsender + ultraPermissionsUserSuffixFinal)
                                .replace("%userprefix%", ultraPermissionsUserPrefixFinal)
                                .replace("%usersuffix%", ultraPermissionsUserSuffixFinal)
                                .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                .replace("&", "§"))));

            } else {

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.DONORCHAT_FORMAT.get(String.class)
                            .replace("%user%", commandsender)
                            .replace("%message%", message)
                            .replace("%displayname%", commandsender)
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-DonorMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_donor().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedDonorChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%userprefix%", "")
                                .replace("%displayname%", commandsender)
                                .replace("%usersuffix%", "")
                                .replace("%message%", message)
                                .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                .replace("&", "§"))));
            }

            if (BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && BungeeConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {

                final TextChannel channel = CleanStaffChat.getInstance().getJda().getTextChannelById(BungeeDiscordConfig.DONOR_CHANNEL_ID.get(String.class));

                if (channel == null) {
                    return;
                }

                if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                    EmbedBuilder embed = new EmbedBuilder();

                    embed.setTitle(BungeeDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);

                    embed.setDescription(BungeeMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                            .replace("%user%", commandsender)
                            .replace("%message%", message)
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName()));

                    embed.setColor(Color.getColor(BungeeDiscordConfig.EMBEDS_DONORCHATCOLOR.get(String.class)));
                    embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                    channel.sendMessageEmbeds(embed.build()).queue();

                } else {

                    channel.sendMessageFormat(BungeeMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                    .replace("%user%", commandsender)
                                    .replace("%message%", message)
                                    .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName()))
                            .queue();

                }
            }

            if (!sender.hasPermission(BungeeConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) {

                PlayerCache.getCooldown().add(((ProxiedPlayer) sender).getUniqueId());
                ProxyServer.getInstance().getScheduler().schedule(plugin, () ->
                        PlayerCache.getCooldown().remove(((ProxiedPlayer) sender).getUniqueId()), BungeeConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);
            }

        } else if (BungeeConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {

            if (!PlayerCache.getMuted_donor().contains("true")) {

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.DONORCHAT_CONSOLE_FORMAT.get(String.class)
                            .replace("%user%", commandsender)
                            .replace("%message%", message)
                            .replace("%displayname%", commandsender)
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", "")
                            .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-DonorMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled_donor().contains(players.getUniqueId()))
                                        && !CleanStaffChat.getInstance().isInBlockedDonorChatServer(players))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_CONSOLE_FORMAT.color()
                                .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                                .replace("%user%", commandsender)
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%displayname%", commandsender)
                                .replace("%server%", "")
                                .replace("%message%", message))));

            } else {
                sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
            }

            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.DONORCHAT_CONSOLE_FORMAT.color()
                    .replace("%prefix%", BungeeMessages.DONORPREFIX.color())
                    .replace("%user%", commandsender)
                    .replace("%displayname%", commandsender)
                    .replace("%userprefix%", "")
                    .replace("%usersuffix%", "")
                    .replace("%server%", "")
                    .replace("%message%", message)));

            if (!BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class) || !BungeeConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class)) {
                return;
            }

            final TextChannel channel = CleanStaffChat.getInstance().getJda().getTextChannelById(BungeeDiscordConfig.DONOR_CHANNEL_ID.get(String.class));

            if (channel == null) {
                return;
            }

            if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {

                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle(BungeeDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class), null);

                embed.setDescription(BungeeMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                        .replace("%user%", commandsender)
                        .replace("%message%", message)
                        .replace("%server%", ""));

                embed.setColor(Color.getColor(BungeeDiscordConfig.EMBEDS_DONORCHATCOLOR.get(String.class)));
                embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);

                channel.sendMessageEmbeds(embed.build()).queue();

            } else {
                channel.sendMessageFormat(BungeeMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                                .replace("%user%", commandsender)
                                .replace("%message%", message)
                                .replace("%server%", ""))
                        .queue();
            }

        } else {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.PLAYER_ONLY.color()
                    .replace("%prefix%", BungeeMessages.DONORPREFIX.color())));
        }
    }
}