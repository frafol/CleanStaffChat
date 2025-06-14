package it.frafol.cleanstaffchat.bukkit.general.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.PlayerCache;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MuteChatCommand extends CommandBase {

    public MuteChatCommand(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(plugin, name, usageMessage, aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSource, @NotNull String commandLabel, @NotNull String[] args) {

        if (args.length == 0) {

            if (!commandSource.hasPermission(SpigotConfig.MUTECHAT_ALL_PERMISSION.get(String.class)) &&
                    !commandSource.hasPermission(SpigotConfig.MUTECHAT_PERMISSION.get(String.class))) {
                commandSource.sendMessage((SpigotMessages.NO_PERMISSION.color()
                        .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())));
                return false;
            }

            String user_prefix = "";
            String user_suffix = "";
            if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null && commandSource instanceof Player) {
                LuckPerms api = LuckPermsProvider.get();
                User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());
                if (user == null) return false;
                final String prefix = user.getCachedData().getMetaData().getPrefix();
                final String suffix = user.getCachedData().getMetaData().getSuffix();
                user_prefix = prefix == null ? "" : prefix;
                user_suffix = suffix == null ? "" : suffix;
            } else if (plugin.getServer().getPluginManager().getPlugin("UltraPermissions") != null && commandSource instanceof Player) {
                final UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();
                final UserList userList = ultraPermissionsAPI.getUsers();
                if (userList.uuid(((Player) commandSource).getUniqueId()).isEmpty()) return false;
                final me.TechsCode.UltraPermissions.storage.objects.User ultraPermissionsUser = userList.uuid(((Player) commandSource).getUniqueId()).get();
                final Optional<String> ultraPermissionsUserPrefix = ultraPermissionsUser.getPrefix();
                final Optional<String> ultraPermissionsUserSuffix = ultraPermissionsUser.getSuffix();
                user_prefix = ultraPermissionsUserPrefix.orElse("");
                user_suffix = ultraPermissionsUserSuffix.orElse("");
            }

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                broadcastMuteChat(commandSource, false);
                commandSource.sendMessage((SpigotMessages.MUTECHAT_DISABLED.color()
                        .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())
                        .replace("%user%", commandSource.getName())
                        .replace("%userprefix%", user_prefix)
                        .replace("%usersuffix%", user_suffix)));
                return false;
            }

            PlayerCache.getMutedservers().add("all");
            broadcastMuteChat(commandSource, true);
            commandSource.sendMessage((SpigotMessages.MUTECHAT_ENABLED.color()
                    .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())
                    .replace("%userprefix%", user_prefix)
                    .replace("%user%", commandSource.getName())
                    .replace("%usersuffix%", user_suffix)));
        } else {
            commandSource.sendMessage((SpigotMessages.MUTECHAT_USAGE.color()
                    .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())));
        }
        return false;
    }

    private void broadcastMuteChat(CommandSender commandSource, boolean activated) {

        String user_prefix = "";
        String user_suffix = "";
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null && commandSource instanceof Player) {
            final LuckPerms api = LuckPermsProvider.get();
            final User user = api.getUserManager().getUser(((Player) commandSource).getUniqueId());
            if (user == null) return;
            final String prefix = user.getCachedData().getMetaData().getPrefix();
            final String suffix = user.getCachedData().getMetaData().getSuffix();
            user_prefix = prefix == null ? "" : prefix;
            user_suffix = suffix == null ? "" : suffix;
        }

        Collection<? extends Player> players;
        if (SpigotConfig.MUTECHAT_BC_ALL.get(Boolean.class)) players = plugin.getServer().getOnlinePlayers();
        else players = plugin.getServer().getOnlinePlayers().stream().filter(player -> player.hasPermission(SpigotConfig.STAFFCHAT_USE_PERMISSION.get(String.class)) && !(PlayerCache.getToggled().contains(player.getUniqueId()))).toList();

        if (activated) {
            for (Player player : players) {
                player.sendMessage((SpigotMessages.MUTECHAT_ENABLED_BC.color()
                        .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())
                        .replace("%userprefix%", user_prefix)
                        .replace("%user%", commandSource.getName())
                        .replace("%usersuffix%", user_suffix)));
            }
            return;
        }

        for (Player player : players) {
            player.sendMessage((SpigotMessages.MUTECHAT_DISABLED_BC.color()
                    .replace("%prefix%", SpigotMessages.GLOBALPREFIX.color())
                    .replace("%userprefix%", user_prefix)
                    .replace("%user%", commandSource.getName())
                    .replace("%usersuffix%", user_suffix)));
        }
    }
}
