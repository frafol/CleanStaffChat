package it.frafol.cleanstaffchat.bungee.enums;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;

public enum BungeeConfig {

    PLAYER_ONLY("messages.console"),

    NO_PERMISSION("messages.no_permission"),

    PREFIX("messages.prefix"),

    STAFFCHAT_FORMAT("messages.staffchat_format"),

    STAFFCHAT_MUTED_ERROR("messages.staffchat_muted_error"),

    STAFF_JOIN_MESSAGE("settings.staff_join_message"),
    STAFF_QUIT_MESSAGE("settings.staff_quit_message"),
    STAFF_JOIN_MESSAGE_FORMAT("messages.staff_join_message_format"),
    STAFF_QUIT_MESSAGE_FORMAT("messages.staff_quit_message_format"),

    STAFFCHAT_USE_PERMISSION("settings.staffchat_use_permission"),

    CONSOLE_PREFIX("settings.console_name"),

    ARGUMENTS("messages.arguments"),

    STAFFCHAT_TALK_MODULE("modules.staffchat_talk_module"),
    STAFFCHAT_TALK_ENABLED("messages.staffchat_talk_enabled"),
    STAFFCHAT_TALK_DISABLED("messages.staffchat_talk_disabled"),

    STAFFCHAT_MUTE_MODULE("modules.staffchat_mute_module"),
    STAFFCHAT_MUTED("messages.staffchat_muted"),
    STAFFCHAT_UNMUTED("messages.staffchat_unmuted"),
    STAFFCHAT_MUTE_PERMISSION("settings.staffchat_mute_permission"),

    STAFFCHAT_TOGGLE_MODULE("modules.staffchat_toggle_module"),
    STAFFCHAT_TOGGLED_ON("messages.staffchat_toggled_on"),
    STAFFCHAT_TOGGLED_OFF("messages.staffchat_toggled_off"),

    STAFFCHAT_RELOAD_PERMISSION("settings.staffchat_reload_permission"),
    RELOADED("messages.reloaded"),

    CONSOLE_CAN_TALK("settings.console_staffchat"),
    STAFFCHAT_TOGGLE_PERMISSION("settings.staffchat_toggle_permission"),

    STAFFCHAT_SWITCH_MODULE("modules.staffchat_switch_module"),
    STAFF_SWITCH_MESSAGE_FORMAT("messages.staff_switch_message_format"),

    MODULE_DISABLED("messages.module_disabled"),

    STATS("modules.stats"),

    UPDATE_CHECK("modules.update_check");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    BungeeConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().get(path));
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }
}
