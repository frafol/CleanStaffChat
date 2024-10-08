package it.frafol.cleanstaffchat.bukkit.enums;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import org.jetbrains.annotations.NotNull;

public enum SpigotVersion {

    VERSION("version");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    SpigotVersion(String path) {
        this.path = path;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().get(path));
    }

    public @NotNull String color() {
        return get(String.class).replace("&", "§");
    }

}