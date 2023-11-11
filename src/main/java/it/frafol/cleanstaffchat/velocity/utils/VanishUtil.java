package it.frafol.cleanstaffchat.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.UUID;

@UtilityClass
public class VanishUtil {

    public boolean isVanished(Player player) {
        return VelocityVanishAPI.isInvisible(player);
    }

    public List<UUID> getVanishedPlayers() {
        return VelocityVanishAPI.getInvisiblePlayers();
    }
}
