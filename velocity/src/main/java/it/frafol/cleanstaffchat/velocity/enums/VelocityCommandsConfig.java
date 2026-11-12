package it.frafol.cleanstaffchat.velocity.enums;

import it.frafol.cleanstaffchat.velocity.CleanStaffChat;

import java.util.List;

public enum VelocityCommandsConfig {

    STAFFCHAT("aliases.staffchat.main"),
    ADMINCHAT("aliases.adminchat.main"),
    DONORCHAT("aliases.donorchat.main"),

    STAFFCHAT_MUTE("aliases.staffchat.mute"),
    ADMINCHAT_MUTE("aliases.adminchat.mute"),
    DONORCHAT_MUTE("aliases.donorchat.mute"),

    STAFFLIST("aliases.stafflist.main"),

    STAFFCHAT_TOGGLE("aliases.staffchat.toggle"),
    ADMINCHAT_TOGGLE("aliases.adminchat.toggle"),
    DONORCHAT_TOGGLE("aliases.donorchat.toggle"),

    MUTECHAT("aliases.mutechat.main"),

    STAFFCHAT_AFK("aliases.staffchat.afk");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    VelocityCommandsConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getAliasesTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return instance.getAliasesTextFile().getConfig().getStringList(path);
    }

}
