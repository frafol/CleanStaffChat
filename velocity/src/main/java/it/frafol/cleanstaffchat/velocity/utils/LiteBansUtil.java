package it.frafol.cleanstaffchat.velocity.utils;

import litebans.api.Database;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class LiteBansUtil {
    public boolean isMuted(UUID uuid, String address) {
        return Database.get().isPlayerMuted(uuid, address);
    }
}
