package it.frafol.cleanstaffchat.bungee.objects;

import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/** Single place for prefix/suffix lookups against LuckPerms and UltraPermissions. */
public final class AffixResolver {

    private AffixResolver() {
    }

    /**
     * @return the player's affixes, {@link Affixes#NONE} without a supported permission plugin,
     * or {@code null} when the permission plugin has no data for the player yet.
     */
    public static Affixes resolve(ProxiedPlayer player) {
        final UUID uuid = player.getUniqueId();

        if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
            final User user = LuckPermsProvider.get().getUserManager().getUser(uuid);
            if (user == null) return null;
            return new Affixes(
                    user.getCachedData().getMetaData().getPrefix(),
                    user.getCachedData().getMetaData().getSuffix());
        }

        if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {
            final UltraPermissionsAPI api = UltraPermissionsBungee.getAPI();
            final UserList userList = api.getUsers();
            if (userList.uuid(uuid).isEmpty()) return null;
            final me.TechsCode.UltraPermissions.storage.objects.User user = userList.uuid(uuid).get();
            return new Affixes(user.getPrefix().orElse(""), user.getSuffix().orElse(""));
        }

        return Affixes.NONE;
    }
}
