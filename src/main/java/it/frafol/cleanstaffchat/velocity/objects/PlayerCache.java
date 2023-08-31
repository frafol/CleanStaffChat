package it.frafol.cleanstaffchat.velocity.objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.UUID;


@UtilityClass
public class PlayerCache {

    @Getter
    private final HashSet<UUID> staffers = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_2 = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_admin = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_2_admin = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_donor = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_2_donor = new HashSet<>();

    @Getter
    private final HashSet<UUID> afk = new HashSet<>();

    @Getter
    private final HashSet<UUID> cooldown = new HashSet<>();

    @Getter
    private final HashSet<String> muted = new HashSet<>();

    @Getter
    private final HashSet<String> muted_admin = new HashSet<>();

    @Getter
    private final HashSet<String> muted_donor = new HashSet<>();

    @Getter
    private final HashSet<String> cooldown_discord = new HashSet<>();

    @Getter
    private final HashSet<String> mutedservers = new HashSet<>();

}