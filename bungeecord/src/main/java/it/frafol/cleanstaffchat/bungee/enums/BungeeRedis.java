package it.frafol.cleanstaffchat.bungee.enums;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import org.jetbrains.annotations.NotNull;

public enum BungeeRedis {

    REDIS_ENABLE("redis.enabled");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    BungeeRedis(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getRedisTextFile().get(path));
    }

}
