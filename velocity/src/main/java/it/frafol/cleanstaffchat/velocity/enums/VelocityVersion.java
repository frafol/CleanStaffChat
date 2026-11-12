package it.frafol.cleanstaffchat.velocity.enums;

import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import org.jetbrains.annotations.NotNull;

public enum VelocityVersion {

    VERSION("version");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    VelocityVersion(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().getConfig().get(path));
    }

}