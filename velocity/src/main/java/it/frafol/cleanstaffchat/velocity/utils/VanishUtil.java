package it.frafol.cleanstaffchat.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class VanishUtil {

    public boolean isVanished(Player player) {
        if (!CleanStaffChat.getInstance().isPremiumVanish()) return false;
        return VelocityVanishAPI.isInvisible(player);
    }

    public boolean isVanished(UUID uuid) {
        if (!CleanStaffChat.getInstance().isPremiumVanish()) return false;
        return VelocityVanishAPI.getInvisiblePlayers().contains(uuid);
    }

    public List<UUID> getVanishedPlayers() {
        if (!CleanStaffChat.getInstance().isPremiumVanish()) return Collections.emptyList();
        return VelocityVanishAPI.getInvisiblePlayers();
    }
}
