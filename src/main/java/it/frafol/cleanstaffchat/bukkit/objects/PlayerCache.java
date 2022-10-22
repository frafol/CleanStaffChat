package it.frafol.cleanstaffchat.bukkit.objects;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.UUID;

@UtilityClass
public class PlayerCache {

    @Getter
    private final List<UUID> toggled = Lists.newArrayList();

    @Getter
    private final List<UUID> toggled_2 = Lists.newArrayList();

    @Getter
    private final List<UUID> toggled_admin = Lists.newArrayList();

    @Getter
    private final List<UUID> toggled_2_admin = Lists.newArrayList();

    @Getter
    private final List<UUID> toggled_donor = Lists.newArrayList();

    @Getter
    private final List<UUID> toggled_2_donor = Lists.newArrayList();

    @Getter
    private final List<UUID> afk = Lists.newArrayList();

    @Getter
    private final List<UUID> cooldown = Lists.newArrayList();

    @Getter
    private final List<String> muted = Lists.newArrayList();

    @Getter
    private final List<String> muted_admin = Lists.newArrayList();

    @Getter
    private final List<String> muted_donor = Lists.newArrayList();

}
