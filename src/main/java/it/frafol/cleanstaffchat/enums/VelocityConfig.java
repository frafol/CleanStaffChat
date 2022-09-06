package it.frafol.cleanstaffchat.enums;

import com.velocitypowered.api.command.CommandSource;
import it.frafol.cleanstaffchat.CleanStaffChat;
import it.frafol.cleanstaffchat.utils.ChatUtil;
import it.frafol.cleanstaffchat.objects.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public enum VelocityConfig {

    PLAYER_ONLY("messages.console"),
    NO_PERMISSION("messages.no_permission"),
    PREFIX("messages.prefix"),
    STAFFCHAT_FORMAT("messages.staffchat_format"),
    STAFFCHAT_MUTED("messages.staffchat_muted"),
    STAFF_JOIN_MESSAGE("settings.staff_join_message"),
    STAFF_QUIT_MESSAGE("settings.staff_quit_message"),
    STAFF_JOIN_MESSAGE_FORMAT("messages.staff_join_message_format"),
    STAFF_QUIT_MESSAGE_FORMAT("messages.staff_quit_message_format"),
    STAFFCHAT_USE_PERMISSION("settings.staffchat_use_permission"),
    DEBUG("settings.debug"),
    CONSOLE_PREFIX("settings.console_name"),

    ARGUMENTS("messages.arguments"),

    STAFFCHAT_MUTE_PERMISSION("settings.staffchat_mute_permission"),

    STAFFCHAT_RELOAD_PERMISSION("settings.staffchat_reload_permission"),
    RELOADED("messages.staffchat_reloaded"),

    CONSOLE_CAN_TALK("settings.console_staffchat"),
    STAFFCHAT_TOGGLE_PERMISSION("settings.staffchat_toggle_permission");

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

    public List<String> getStringList() {
        return instance.getConfigTextFile().getConfig().getStringList(path);
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }

    public void send(CommandSource commandSource, Placeholder... placeholders) {

        if (ChatUtil.getString(this).equals("")) {
            return;
        }

        commandSource.sendMessage(Component.text(ChatUtil.getFormattedString(this, placeholders)));
    }

    public void sendList(CommandSource commandSource, Placeholder... placeholder) {
        ChatUtil.sendFormattedList(this, commandSource, placeholder);
    }

    public static TextComponent color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
