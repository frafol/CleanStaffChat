package it.frafol.cleanstaffchat.hytale.enums;

import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.hytale.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.hytale.general.commands.MuteChatCommand;
import it.frafol.cleanstaffchat.hytale.staffchat.commands.*;

import java.util.List;

public enum HytaleCommandsConfig {

    STAFFCHAT(StaffChatCommand.class, "aliases.staffchat.main"),
    ADMINCHAT(AdminChatCommand.class, "aliases.adminchat.main"),
    DONORCHAT(DonorChatCommand.class, "aliases.donorchat.main"),
    STAFFLIST(StaffListCommand.class, "aliases.stafflist.main"),
    STAFFCHAT_MUTE(MuteCommand.class, "aliases.staffchat.mute"),
    ADMINCHAT_MUTE(it.frafol.cleanstaffchat.hytale.adminchat.commands.MuteCommand.class, "aliases.adminchat.mute"),
    DONORCHAT_MUTE(it.frafol.cleanstaffchat.hytale.donorchat.commands.MuteCommand.class, "aliases.donorchat.mute"),
    STAFFCHAT_TOGGLE(ToggleCommand.class, "aliases.staffchat.toggle"),
    MUTECHAT(MuteChatCommand.class, "aliases.mutechat.main"),
    ADMINCHAT_TOGGLE(it.frafol.cleanstaffchat.hytale.adminchat.commands.ToggleCommand.class, "aliases.adminchat.toggle"),
    DONORCHAT_TOGGLE(it.frafol.cleanstaffchat.hytale.donorchat.commands.ToggleCommand.class, "aliases.donorchat.toggle"),
    STAFFCHAT_AFK(AFKCommand.class, "aliases.staffchat.afk");

    private final String path;
    private final Class<?> commandClass;

    HytaleCommandsConfig(Class<?> commandClass, String path) {
        this.commandClass = commandClass;
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        Object value = CleanStaffChat.getInstance().getAliasesTextFile().get(path);
        if (value == null) return null;
        return clazz.cast(value);
    }

    public List<String> getStringList() {
        List<String> list = CleanStaffChat.getInstance().getAliasesTextFile().getStringList(path);
        return list == null ? List.of() : list;
    }

    public String color() {
        String value = get(String.class);
        return value == null ? "" : value.replace("&", "§");
    }

    public Class<?> getCommandClass() {
        return this.commandClass;
    }

    public static List<HytaleCommandsConfig> getStaffChatCommands() {
        return List.of(STAFFCHAT, STAFFCHAT_MUTE, STAFFCHAT_AFK, STAFFCHAT_TOGGLE);
    }

    public static List<HytaleCommandsConfig> getStaffListCommands() {
        return List.of(STAFFLIST);
    }

    public static List<HytaleCommandsConfig> getMuteChatCommands() {
        return List.of(MUTECHAT);
    }

    public static List<HytaleCommandsConfig> getAdminChatCommands() {
        return List.of(ADMINCHAT, ADMINCHAT_MUTE, ADMINCHAT_TOGGLE);
    }

    public static List<HytaleCommandsConfig> getDonorChatCommands() {
        return List.of(DONORCHAT, DONORCHAT_MUTE, DONORCHAT_TOGGLE);
    }
}