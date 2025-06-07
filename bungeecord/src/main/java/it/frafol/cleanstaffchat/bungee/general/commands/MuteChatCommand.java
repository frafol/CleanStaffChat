package it.frafol.cleanstaffchat.bungee.general.commands;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MuteChatCommand extends Command implements TabExecutor {

    public MuteChatCommand() {
        super(BungeeCommandsConfig.MUTECHAT.getStringList().get(0),"", BungeeCommandsConfig.MUTECHAT.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender commandSource, String[] args) {

        if (args.length == 0) {

            if (!commandSource.hasPermission(BungeeConfig.MUTECHAT_ALL_PERMISSION.get(String.class))) {
                commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            String user_prefix = "";
            String user_suffix = "";
            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null && commandSource instanceof ProxiedPlayer) {
                final LuckPerms api = LuckPermsProvider.get();
                final User user = api.getUserManager().getUser(((ProxiedPlayer) commandSource).getUniqueId());
                if (user == null) return;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                user_prefix = prefix == null ? "" : prefix;
                user_suffix = suffix == null ? "" : suffix;

            } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null && commandSource instanceof ProxiedPlayer) {
                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();
                if (!userList.uuid(((ProxiedPlayer) commandSource).getUniqueId()).isPresent()) return;
                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((ProxiedPlayer) commandSource).getUniqueId()).get();
                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                user_prefix = ultraPermissionsUserPrefix.orElse("");
                user_suffix = ultraPermissionsUserSuffix.orElse("");
            }

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                broadcastMuteChat(commandSource, "all", false);
                commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_DISABLED.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%server%", "")));
                return;
            }

            PlayerCache.getMutedservers().add("all");
            broadcastMuteChat(commandSource, "all", true);
            commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_ENABLED.color()
                    .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                    .replace("%userprefix%", user_prefix)
                    .replace("%usersuffix%", user_suffix)
                    .replace("%server%", "")));
            return;
        }

        if (args.length == 1) {

            if (!(commandSource instanceof ProxiedPlayer)) {
                commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.PLAYER_ONLY.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            String user_prefix = "";
            String user_suffix = "";
            if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
                final LuckPerms api = LuckPermsProvider.get();
                final User user = api.getUserManager().getUser(((ProxiedPlayer) commandSource).getUniqueId());
                if (user == null) return;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                user_prefix = prefix == null ? "" : prefix;
                user_suffix = suffix == null ? "" : suffix;
            } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {
                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();
                if (!userList.uuid(((ProxiedPlayer) commandSource).getUniqueId()).isPresent()) return;
                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((ProxiedPlayer) commandSource).getUniqueId()).get();
                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                user_prefix = ultraPermissionsUserPrefix.orElse("");
                user_suffix = ultraPermissionsUserSuffix.orElse("");
            }

            if (!commandSource.hasPermission(BungeeConfig.MUTECHAT_PERMISSION.get(String.class))) {
                commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            String server = args[0];

            if (server.equalsIgnoreCase("all")) {

                if (!commandSource.hasPermission(BungeeConfig.MUTECHAT_ALL_PERMISSION.get(String.class))) {
                    commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                            .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                    return;
                }

                if (PlayerCache.getMutedservers().contains("all")) {
                    PlayerCache.getMutedservers().remove("all");
                    broadcastMuteChat(commandSource, server, false);
                    commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_DISABLED.color()
                            .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                            .replace("%user%", commandSource.getName())
                            .replace("%userprefix%", user_prefix)
                            .replace("%usersuffix%", user_suffix)
                            .replace("%server%", ((ProxiedPlayer) commandSource).getServer().getInfo().getName())));
                    return;
                }

                PlayerCache.getMutedservers().add("all");
                broadcastMuteChat(commandSource, server, true);
                commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_ENABLED.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                        .replace("%user%", commandSource.getName())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%server%", ((ProxiedPlayer) commandSource).getServer().getInfo().getName())));
                return;
            }

            if (!CleanStaffChat.getInstance().getProxy().getServers().containsKey(server)) {
                commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.SERVER_NOT_FOUND.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
                return;
            }

            if (PlayerCache.getMutedservers().contains(server)) {
                PlayerCache.getMutedservers().remove(server);
                broadcastMuteChat(commandSource, server, false);
                commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_DISABLED.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                        .replace("%user%", commandSource.getName())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%server%", server)));
                return;
            }

            PlayerCache.getMutedservers().add(server);
            broadcastMuteChat(commandSource, server, true);
            commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_ENABLED.color()
                    .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                    .replace("%user%", commandSource.getName())
                    .replace("%userprefix%", user_prefix)
                    .replace("%usersuffix%", user_suffix)
                    .replace("%server%", server)));

        } else {
            commandSource.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_USAGE.color()
                    .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())));
        }
    }

    private void broadcastMuteChat(CommandSender commandSource, String server, boolean activated) {

        String user_prefix = "";
        String user_suffix = "";
        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(((ProxiedPlayer) commandSource).getUniqueId());
            if (user == null) return;
            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;
        } else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {
            final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissionsBungee.getAPI();
            final UserList userList = ultraPermissionsAPI.getUsers();
            if (!userList.uuid(((ProxiedPlayer) commandSource).getUniqueId()).isPresent()) return;
            final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((ProxiedPlayer) commandSource).getUniqueId()).get();
            final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
            final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
            user_prefix = ultraPermissionsUserPrefix.orElse("");
            user_suffix = ultraPermissionsUserSuffix.orElse("");
        }

        if (activated) {
            if (server.equals("all")) {
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    player.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_ENABLED_BC.color()
                            .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                            .replace("%user%", commandSource.getName())
                            .replace("%userprefix%", user_prefix)
                            .replace("%usersuffix%", user_suffix)
                            .replace("%server%", server)));
                }
            } else {
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    if (player.getServer() == null) continue;
                    if (!player.getServer().getInfo().getName().equals(server)) continue;
                    player.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_ENABLED_BC.color()
                            .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                            .replace("%user%", commandSource.getName())
                            .replace("%userprefix%", user_prefix)
                            .replace("%usersuffix%", user_suffix)
                            .replace("%server%", server)));
                }
            }
            return;
        }

        if (server.equals("all")) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                player.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_DISABLED_BC.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                        .replace("%user%", commandSource.getName())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%server%", server)));
            }
        } else {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.getServer() == null) continue;
                if (!player.getServer().getInfo().getName().equals(server)) continue;
                player.sendMessage(TextComponent.fromLegacy(BungeeMessages.MUTECHAT_DISABLED_BC.color()
                        .replace("%prefix%", BungeeMessages.GLOBALPREFIX.color())
                        .replace("%user%", commandSource.getName())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)
                        .replace("%server%", server)));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String @NotNull [] args) {

        if (args.length != 1) {
            return Collections.emptyList();
        }

        String partialName = args[0].toLowerCase();

        List<String> completions = new ArrayList<>();
        for (String servers : ProxyServer.getInstance().getServers().keySet()) {
            if (servers.toLowerCase().startsWith(partialName)) {
                completions.add(servers);
            }
        }

        return completions;
    }
}
