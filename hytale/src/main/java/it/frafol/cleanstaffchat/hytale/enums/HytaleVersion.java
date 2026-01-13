package it.frafol.cleanstaffchat.hytale.enums;

import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import org.jetbrains.annotations.NotNull;

public enum HytaleVersion {

    VERSION("version");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    HytaleVersion(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().get(path));
    }
}