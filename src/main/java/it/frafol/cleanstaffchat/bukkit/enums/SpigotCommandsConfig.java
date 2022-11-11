package it.frafol.cleanstaffchat.bukkit.enums;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;

import java.util.List;

public enum SpigotCommandsConfig {

    STAFFCHAT("aliases.staffchat.main"),
    ADMINCHAT("aliases.adminchat.main"),
    DONORCHAT("aliases.donorchat.main"),

    STAFFCHAT_MUTE("aliases.staffchat.mute"),
    ADMINCHAT_MUTE("aliases.adminchat.mute"),
    DONORCHAT_MUTE("aliases.donorchat.mute"),

    STAFFCHAT_TOGGLE("aliases.staffchat.toggle"),
    ADMINCHAT_TOGGLE("aliases.adminchat.toggle"),
    DONORCHAT_TOGGLE("aliases.donorchat.toggle"),

    STAFFCHAT_AFK("aliases.staffchat.afk");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    SpigotCommandsConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getAliasesTextFile().get(path));
    }

    public List<String> getStringList() {
        return instance.getAliasesTextFile().getStringList(path);
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }
}
