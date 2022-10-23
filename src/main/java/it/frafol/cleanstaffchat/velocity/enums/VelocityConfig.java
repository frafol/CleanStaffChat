package it.frafol.cleanstaffchat.velocity.enums;

import com.velocitypowered.api.command.CommandSource;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public enum VelocityConfig {

    PLAYER_ONLY("messages.console"),

    NO_PERMISSION("messages.no_permission"),

    PREFIX("messages.staffchat.prefix"),
    ADMINPREFIX("messages.adminchat.prefix"),
    DONORPREFIX("messages.donorchat.prefix"),

    DONORCHAT_COOLDOWN_MESSAGE("messages.donorchat.cooldown"),

    STAFFCHAT_FORMAT("messages.staffchat.staffchat_format"),
    ADMINCHAT_FORMAT("messages.adminchat.adminchat_format"),
    DONORCHAT_FORMAT("messages.donorchat.donorchat_format"),

    STAFFCHAT_MUTED_ERROR("messages.staffchat.staffchat_muted_error"),
    ADMINCHAT_MUTED_ERROR("messages.adminchat.adminchat_muted_error"),
    DONORCHAT_MUTED_ERROR("messages.donorchat.donorchat_muted_error"),

    STAFFCHAT_JOIN_LEAVE_ALL("settings.staff_join_all_players"),
    STAFFCHAT_QUIT_ALL("settings.staff_quit_all_players"),

    STAFFCHAT_AFK_MODULE("modules.staffchat_afk_module"),
    STAFFCHAT_AFK_ON("messages.staff_afk_on_message_format"),
    STAFFCHAT_AFK_OFF("messages.staff_afk_off_message_format"),
    STAFFCHAT_NO_AFK_ONCHANGE_SERVER("settings.staffchat.staff_disable_afk_on_move"),
    STAFFCHAT_AFK_PERMISSION("settings.staffchat.staffchat_afk_permission"),

    STAFF_JOIN_MESSAGE("settings.staffchat.staff_join_message"),
    STAFF_QUIT_MESSAGE("settings.staffchat.staff_quit_message"),
    STAFF_JOIN_MESSAGE_FORMAT("messages.staffchat.staff_join_message_format"),
    STAFF_QUIT_MESSAGE_FORMAT("messages.staffchat.staff_quit_message_format"),

    STAFFCHAT_USE_PERMISSION("settings.staffchat.staffchat_use_permission"),
    ADMINCHAT_USE_PERMISSION("settings.adminchat.adminchat_use_permission"),
    DONORCHAT_USE_PERMISSION("settings.donorchat.donorchat_use_permission"),

    PREVENT_COLOR_CODES("settings.prevent_color_codes"),
    COLOR_CODES("messages.deny_color_codes"),
    DONOR_TIMER("settings.donorchat.cooldown"),

    CONSOLE_PREFIX("settings.console_name"),

    ARGUMENTS("messages.staffchat.arguments"),
    ADMINARGUMENTS("messages.adminchat.arguments"),
    DONORARGUMENTS("messages.donorchat.arguments"),

    STAFFCHAT_TALK_MODULE("modules.staffchat_talk_module"),
    ADMINCHAT_TALK_MODULE("modules.adminchat_talk_module"),
    DONORCHAT_TALK_MODULE("modules.donorchat_talk_module"),
    STAFFCHAT_TALK_ENABLED("messages.staffchat.staffchat_talk_enabled"),
    STAFFCHAT_TALK_DISABLED("messages.staffchat.staffchat_talk_disabled"),
    ADMINCHAT_TALK_ENABLED("messages.adminchat.adminchat_talk_enabled"),
    ADMINCHAT_TALK_DISABLED("messages.adminchat.adminchat_talk_disabled"),
    DONORCHAT_TALK_ENABLED("messages.donorchat.donorchat_talk_enabled"),
    DONORCHAT_TALK_DISABLED("messages.donorchat.donorchat_talk_disabled"),

    STAFFCHAT_MUTE_MODULE("modules.staffchat_mute_module"),
    ADMINCHAT_MUTE_MODULE("modules.adminchat_mute_module"),
    DONORCHAT_MUTE_MODULE("modules.donorchat_mute_module"),
    STAFFCHAT_MUTED("messages.staffchat.staffchat_muted"),
    STAFFCHAT_UNMUTED("messages.staffchat.staffchat_unmuted"),
    ADMINCHAT_MUTED("messages.adminchat.adminchat_muted"),
    ADMINCHAT_UNMUTED("messages.adminchat.adminchat_unmuted"),
    DONORCHAT_MUTED("messages.donorchat.donorchat_muted"),
    DONORCHAT_UNMUTED("messages.donorchat.donorchat_unmuted"),
    STAFFCHAT_MUTE_PERMISSION("settings.staffchat.staffchat_mute_permission"),
    ADMINCHAT_MUTE_PERMISSION("settings.adminchat.adminchat_mute_permission"),
    DONORCHAT_MUTE_PERMISSION("settings.donorchat.donorchat_mute_permission"),

    STAFFCHAT_TOGGLE_MODULE("modules.staffchat_toggle_module"),
    ADMINCHAT_TOGGLE_MODULE("modules.adminchat_toggle_module"),
    DONORCHAT_TOGGLE_MODULE("modules.donorchat_toggle_module"),
    STAFFCHAT_TOGGLED_ON("messages.staffchat.staffchat_toggled_on"),
    STAFFCHAT_TOGGLED_OFF("messages.staffchat.staffchat_toggled_off"),
    ADMINCHAT_TOGGLED_ON("messages.adminchat.adminchat_toggled_on"),
    ADMINCHAT_TOGGLED_OFF("messages.adminchat.adminchat_toggled_off"),
    DONORCHAT_TOGGLED_ON("messages.donorchat.donorchat_toggled_on"),
    DONORCHAT_TOGGLED_OFF("messages.donorchat.donorchat_toggled_off"),

    STAFFCHAT_RELOAD_PERMISSION("settings.reload_permission"),
    RELOADED("messages.reloaded"),

    CONSOLE_CAN_TALK("settings.console_staffchat"),
    STAFFCHAT_TOGGLE_PERMISSION("settings.staffchat.staffchat_toggle_permission"),
    ADMINCHAT_TOGGLE_PERMISSION("settings.adminchat.adminchat_toggle_permission"),
    DONORCHAT_TOGGLE_PERMISSION("settings.donorchat.donorchat_toggle_permission"),

    STAFFCHAT_SWITCH_MODULE("modules.staffchat_switch_module"),
    STAFFCHAT_SWITCH_ALL("settings.staffchat.staff_switch_all_players"),
    STAFF_SWITCH_MESSAGE_FORMAT("messages.staffchat.staff_switch_message_format"),

    MODULE_DISABLED("messages.module_disabled"),

    STATS("modules.stats"),

    UPDATE_CHECK("modules.update_check"),

    STAFFCHAT("settings.use_staffchat"),
    ADMINCHAT("settings.use_adminchat"),
    DONORCHAT("settings.use_donorchat");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    VelocityConfig(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().getConfig().get(path));
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }

    public void send(CommandSource commandSource, Placeholder... placeholders) {

        if (ChatUtil.getString(this).equals("")) {
            return;
        }

        commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(ChatUtil.getFormattedString(this, placeholders)));
        
    }
}
