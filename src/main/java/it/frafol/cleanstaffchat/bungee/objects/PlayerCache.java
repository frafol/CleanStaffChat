package it.frafol.cleanstaffchat.bungee.objects;

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
    private final List<UUID> afk = Lists.newArrayList();

    @Getter
    private final List<String> muted = Lists.newArrayList();

}
