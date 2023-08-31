package it.frafol.cleanstaffchat.velocity.enums;

import it.frafol.cleanstaffchat.velocity.CleanStaffChat;

import java.util.List;

public enum VelocityServers {

    STAFFCHAT_ENABLE("settings.staffchat_enable"),
    ADMINCHAT_ENABLE("settings.adminchat_enable"),
    DONORCHAT_ENABLE("settings.donorchat_enable"),

    SC_BLOCKED_SRV("settings.staffchat_blocked_servers"),
    AC_BLOCKED_SRV("settings.adminchat_blocked_servers"),
    DC_BLOCKED_SRV("settings.donorchat_blocked_servers");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    VelocityServers(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getServersTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return instance.getServersTextFile().getConfig().getStringList(path);
    }

}
