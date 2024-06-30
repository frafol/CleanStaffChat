package it.frafol.cleanstaffchat.velocity.enums;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum VelocityMessages {

    PLAYER_ONLY("messages.console"),

    NO_PERMISSION("messages.no_permission"),

    PREFIX("messages.staffchat.prefix"),
    ADMINPREFIX("messages.adminchat.prefix"),
    DONORPREFIX("messages.donorchat.prefix"),

    DONORCHAT_COOLDOWN_MESSAGE("messages.donorchat.cooldown"),

    STAFFCHAT_FORMAT("messages.staffchat.staffchat_format"),
    STAFFCHAT_CONSOLE_FORMAT("messages.staffchat.staffchat_console_format"),

    ADMINCHAT_FORMAT("messages.adminchat.adminchat_format"),
    ADMINCHAT_CONSOLE_FORMAT("messages.adminchat.adminchat_console_format"),

    DONORCHAT_FORMAT("messages.donorchat.donorchat_format"),
    DONORCHAT_CONSOLE_FORMAT("messages.donorchat.donorchat_console_format"),

    GLOBALPREFIX("messages.globalchat.prefix"),
    MUTECHAT_USAGE("messages.globalchat.mutechat_usage"),
    MUTECHAT_ENABLED("messages.globalchat.mutechat_enabled"),
    MUTECHAT_DISABLED("messages.globalchat.mutechat_disabled"),
    SERVER_NOT_FOUND("messages.globalchat.server_not_found"),

    STAFFCHAT_MUTED_ERROR_DISCORD("messages.chat_muted_error_discord"),
    STAFFCHAT_MUTED_ERROR("messages.chat_muted_error"),
    ADMINCHAT_MUTED_ERROR("messages.chat_muted_error"),
    DONORCHAT_MUTED_ERROR("messages.chat_muted_error"),

    STAFFCHAT_AFK_ON("messages.staffchat.staff_afk_on_message_format"),
    STAFFCHAT_AFK_OFF("messages.staffchat.staff_afk_off_message_format"),

    STAFFCHAT_FORMAT_DISCORD("messages.staffchat.discord.staffchat_to_discord_format"),
    DONORCHAT_FORMAT_DISCORD("messages.donorchat.discord.donorchat_to_discord_format"),
    ADMINCHAT_FORMAT_DISCORD("messages.adminchat.discord.adminchat_to_discord_format"),

    DISCORDLIST_HEADER("messages.stafflist.discord.header"),
    DISCORDLIST_FORMAT("messages.stafflist.discord.format"),
    DISCORDLIST_FOOTER("messages.stafflist.discord.footer"),
    DISCORDLIST_NOBODY("messages.stafflist.discord.nobody"),

    STAFFLIST_AFK("messages.stafflist.afk"),
    DISCORDLIST_AFK("messages.stafflist.discord.afk"),

    STAFF_DISCORD_JOIN_MESSAGE_FORMAT("messages.staffchat.discord.discord_join_message_format"),
    STAFF_DISCORD_QUIT_MESSAGE_FORMAT("messages.staffchat.discord.discord_quit_message_format"),
    STAFF_DISCORD_SWITCH_MESSAGE_FORMAT("messages.staffchat.discord.discord_switch_message_format"),
    STAFF_DISCORD_AFK_ON_MESSAGE_FORMAT("messages.staffchat.discord.discord_afk_on_message_format"),
    STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT("messages.staffchat.discord.discord_afk_off_message_format"),
    STAFF_JOIN_MESSAGE_FORMAT("messages.staffchat.staff_join_message_format"),
    STAFF_QUIT_MESSAGE_FORMAT("messages.staffchat.staff_quit_message_format"),

    COLOR_CODES("messages.deny_color_codes"),

    CONSOLE_PREFIX("settings.console_name"),

    ARGUMENTS("messages.staffchat.arguments"),
    ADMINARGUMENTS("messages.adminchat.arguments"),
    DONORARGUMENTS("messages.donorchat.arguments"),

    STAFFCHAT_TALK_ENABLED("messages.staffchat.staffchat_talk_enabled"),
    STAFFCHAT_TALK_DISABLED("messages.staffchat.staffchat_talk_disabled"),
    ADMINCHAT_TALK_ENABLED("messages.adminchat.adminchat_talk_enabled"),
    ADMINCHAT_TALK_DISABLED("messages.adminchat.adminchat_talk_disabled"),
    DONORCHAT_TALK_ENABLED("messages.donorchat.donorchat_talk_enabled"),
    DONORCHAT_TALK_DISABLED("messages.donorchat.donorchat_talk_disabled"),

    STAFFCHAT_MUTED("messages.staffchat.staffchat_muted"),
    STAFFCHAT_UNMUTED("messages.staffchat.staffchat_unmuted"),
    ADMINCHAT_MUTED("messages.adminchat.adminchat_muted"),
    ADMINCHAT_UNMUTED("messages.adminchat.adminchat_unmuted"),
    DONORCHAT_MUTED("messages.donorchat.donorchat_muted"),
    DONORCHAT_UNMUTED("messages.donorchat.donorchat_unmuted"),

    STAFFCHAT_TOGGLED_ON("messages.staffchat.staffchat_toggled_on"),
    STAFFCHAT_TOGGLED_OFF("messages.staffchat.staffchat_toggled_off"),
    ADMINCHAT_TOGGLED_ON("messages.adminchat.adminchat_toggled_on"),
    ADMINCHAT_TOGGLED_OFF("messages.adminchat.adminchat_toggled_off"),
    DONORCHAT_TOGGLED_ON("messages.donorchat.donorchat_toggled_on"),
    DONORCHAT_TOGGLED_OFF("messages.donorchat.donorchat_toggled_off"),

    RELOADED("messages.reloaded"),

    STAFF_SWITCH_MESSAGE_FORMAT("messages.staffchat.staff_switch_message_format"),

    MODULE_DISABLED("messages.module_disabled"),

    DISCORD_STAFF_FORMAT("messages.staffchat.discord.staffchat_discord_format"),
    DISCORD_DONOR_FORMAT("messages.donorchat.discord.donorchat_discord_format"),
    DISCORD_ADMIN_FORMAT("messages.adminchat.discord.adminchat_discord_format"),

    LIST_HEADER("messages.stafflist.header"),
    LIST_FORMAT("messages.stafflist.format"),
    LIST_FOOTER("messages.stafflist.footer"),
    LIST_NONE("messages.stafflist.nobody"),
    LIST_USAGE("messages.stafflist.usage"),

    UPDATE("messages.new_version"),

    DONORCHAT_COOLDOWN_ERROR_DISCORD("messages.donorchat.discord.discord_cooldown_message");

    private final String path;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    VelocityMessages(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getMessagesTextFile().getConfig().get(path));
    }

    public String color() {
        String hex = convertHexColors(get(String.class));
        return hex.replace("&", "§");
    }

    private String convertHexColors(String message) {

        if (!containsHexColor(message)) {
            return message;
        }

        Pattern hexPattern = Pattern.compile("(#[A-Fa-f0-9]{6}|<#[A-Fa-f0-9]{6}>|&#[A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group();
            String colorCode = hexCode.substring(1, 7);
            if (hexCode.startsWith("<#") && hexCode.endsWith(">")) {
                colorCode = hexCode.substring(2, 8);
            } else if (hexCode.startsWith("&#")) {
                colorCode = hexCode.substring(2, 8);
            }
            String minecraftColorCode = translateHexToMinecraftColorCode(colorCode);
            matcher.appendReplacement(buffer, minecraftColorCode);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String translateHexToMinecraftColorCode(String hex) {
        char[] chars = hex.toCharArray();
        return "§x" +
                '§' + chars[0] +
                '§' + chars[1] +
                '§' + chars[2] +
                '§' + chars[3] +
                '§' + chars[4] +
                '§' + chars[5];
    }

    private boolean containsHexColor(String message) {
        String[] hexColorPattern;
        if (VelocityConfig.MINIMESSAGE.get(Boolean.class)) {
            hexColorPattern = new String[]{"#[a-fA-F0-9]{6}", "&#[a-fA-F0-9]{6}"};
        } else {
            hexColorPattern = new String[]{"#[a-fA-F0-9]{6}", "&#[a-fA-F0-9]{6}", "<#[a-fA-F0-9]]{6}>"};
        }

        for (String pattern : hexColorPattern) {
            if (Pattern.compile(pattern).matcher(message).find()) {
                return true;
            }
        }
        return false;
    }

    private MiniMessage getMiniMessage() {
        return MiniMessage.miniMessage();
    }

    public void send(CommandSource commandSource, Placeholder... placeholders) {

        if (ChatUtil.getString(this).isEmpty()) {
            return;
        }

        if (commandSource instanceof Player) {
            if (VelocityConfig.MINIMESSAGE.get(Boolean.class)) {

                if (ChatUtil.hasColorCodes(get(String.class))) {
                    instance.getLogger().error("Legacy color codes are not supported in MiniMessage mode.");
                    instance.getLogger().error("Please remove them from the message: {} in the messages.yml file.", getPath());
                    commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(ChatUtil.getFormattedString((Player) commandSource,this, placeholders)));
                    return;
                }

                commandSource.sendMessage(getMiniMessage().deserialize(ChatUtil.getFormattedString((Player) commandSource, this, placeholders)));
                return;
            }

            commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(ChatUtil.getFormattedString((Player) commandSource,this, placeholders)));
            return;
        }

        if (VelocityConfig.MINIMESSAGE.get(Boolean.class)) {

            if (ChatUtil.hasColorCodes(get(String.class))) {
                instance.getLogger().error("Legacy color codes are not supported in MiniMessage mode.");
                instance.getLogger().error("Please remove them from the message: {} in the messages.yml file.", getPath());
                commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(ChatUtil.getFormattedString(this, placeholders)));
                return;
            }

            commandSource.sendMessage(getMiniMessage().deserialize(ChatUtil.getFormattedString(this, placeholders)));
            return;
        }

        commandSource.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(ChatUtil.getFormattedString(this, placeholders)));
    }
}
