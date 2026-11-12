package it.frafol.cleanstaffchat.bukkit.objects;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.storage.collection.UserList;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Single place where prefix/suffix lookups against the supported permission plugins happen.
 */
public final class AffixResolver {

    private AffixResolver() {
    }

    /**
     * @return the player's affixes, {@link Affixes#NONE} when no supported permission plugin is installed,
     * or {@code null} when the permission plugin has no data (yet) for this player.
     */
    public static Affixes resolve(Player player) {
        final UUID uuid = player.getUniqueId();

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            final User user = LuckPermsProvider.get().getUserManager().getUser(uuid);
            if (user == null) return null;
            return new Affixes(
                    user.getCachedData().getMetaData().getPrefix(),
                    user.getCachedData().getMetaData().getSuffix());
        }

        if (Bukkit.getPluginManager().getPlugin("UltraPermissions") != null) {
            final UserList userList = UltraPermissions.getAPI().getUsers();
            if (!userList.uuid(uuid).isPresent()) return null;
            final me.TechsCode.UltraPermissions.storage.objects.User user = userList.uuid(uuid).get();
            return new Affixes(user.getPrefix().orElse(""), user.getSuffix().orElse(""));
        }

        return Affixes.NONE;
    }
}
