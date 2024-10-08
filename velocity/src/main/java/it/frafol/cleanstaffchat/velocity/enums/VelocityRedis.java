package it.frafol.cleanstaffchat.velocity.enums;

import it.frafol.cleanstaffchat.velocity.CleanStaffChat;

public enum VelocityRedis {

    REDIS_ENABLE("redis.enabled");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    VelocityRedis(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getRedisTextFile().getConfig().get(path));
    }

}
