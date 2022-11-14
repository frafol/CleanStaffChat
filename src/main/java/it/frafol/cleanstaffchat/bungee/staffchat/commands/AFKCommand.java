package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.enums.BungeeRedis;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AFKCommand extends Command {

    public AFKCommand() {
        super(BungeeCommandsConfig.STAFFCHAT_AFK.getStringList().get(0), "", BungeeCommandsConfig.STAFFCHAT_AFK.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.PLAYER_ONLY.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!BungeeConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class)) {

            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));

            return;

        }

        if (!sender.hasPermission(BungeeConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {

            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));

            return;

        }

        if (!PlayerCache.getAfk().contains(((ProxiedPlayer) sender).getUniqueId())) {
            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((ProxiedPlayer) sender).getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.STAFFCHAT_AFK_ON.get(String.class)
                            .replace("%user%", sender.getName())
                            .replace("%displayname%", user_prefix + sender.getName() + user_suffix)
                            .replace("%userprefix%", user_prefix)
                            .replace("%usersuffix%", user_suffix)
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffAFKMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_AFK_ON.color()
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%displayname%", user_prefix + sender.getName() + user_suffix)
                                .replace("%userprefix%", user_prefix)
                                .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                .replace("%usersuffix%", user_suffix))));

            } else {

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.STAFFCHAT_AFK_ON.get(String.class)
                            .replace("%user%", sender.getName())
                            .replace("%displayname%", sender.getName())
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffAFKMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_AFK_ON.color()
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                .replace("%displayname%", sender.getName()))));

            }

            PlayerCache.getAfk().add(((ProxiedPlayer) sender).getUniqueId());

        } else {

            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                final LuckPerms api = LuckPermsProvider.get();

                final User user = api.getUserManager().getUser(((ProxiedPlayer) sender).getUniqueId());

                assert user != null;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                final String user_prefix = prefix == null ? "" : prefix;
                final String user_suffix = suffix == null ? "" : suffix;

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.STAFFCHAT_AFK_OFF.get(String.class)
                            .replace("%user%", sender.getName())
                            .replace("%displayname%", user_prefix + sender.getName() + user_suffix)
                            .replace("%userprefix%", user_prefix)
                            .replace("%usersuffix%", user_suffix)
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffAFKMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%displayname%", user_prefix + sender.getName() + user_suffix)
                                .replace("%userprefix%", user_prefix)
                                .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                .replace("%usersuffix%", user_suffix))));

            } else {

                if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

                    final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

                    final String final_message = BungeeMessages.STAFFCHAT_AFK_OFF.get(String.class)
                            .replace("%user%", sender.getName())
                            .replace("%displayname%", sender.getName())
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                            .replace("%prefix%", BungeeMessages.PREFIX.color())
                            .replace("&", "§");

                    redisBungeeAPI.sendChannelMessage("CleanStaffChat-StaffAFKMessage-RedisBungee", final_message);

                    return;

                }

                CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                        && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                        .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeMessages.STAFFCHAT_AFK_OFF.color()
                                .replace("%prefix%", BungeeMessages.PREFIX.color())
                                .replace("%user%", sender.getName())
                                .replace("%userprefix%", "")
                                .replace("%usersuffix%", "")
                                .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                .replace("%displayname%", sender.getName()))));

            }

            PlayerCache.getAfk().remove(((ProxiedPlayer) sender).getUniqueId());

        }
    }
}