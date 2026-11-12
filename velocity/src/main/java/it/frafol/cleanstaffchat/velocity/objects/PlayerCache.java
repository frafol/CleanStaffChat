package it.frafol.cleanstaffchat.velocity.objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime state that is not tied to a chat channel (per-channel state lives in ChatChannel).
 * All sets are thread-safe: chat events and Discord callbacks run on different threads.
 */
@UtilityClass
public class PlayerCache {

    @Getter
    private final Set<UUID> staffers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<UUID> afk = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<UUID> cooldown = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<String> cooldown_discord = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<String> mutedservers = ConcurrentHashMap.newKeySet();
}
