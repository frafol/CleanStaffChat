package it.frafol.cleanstaffchat.velocity.objects;

import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

/** Single place for prefix/suffix lookups against LuckPerms. */
public final class AffixResolver {

    private AffixResolver() {
    }

    /**
     * @return the player's affixes, {@link Affixes#NONE} without LuckPerms,
     * or {@code null} when LuckPerms has no data for the player yet.
     */
    public static Affixes resolve(Player player) {
        if (!CleanStaffChat.getInstance().getServer().getPluginManager().isLoaded("luckperms")) {
            return Affixes.NONE;
        }

        final User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;

        return new Affixes(
                user.getCachedData().getMetaData().getPrefix(),
                user.getCachedData().getMetaData().getSuffix());
    }
}
