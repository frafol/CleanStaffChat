package it.frafol.cleanstaffchat.bukkit.enums;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.adminchat.commands.AdminChatCommand;
import it.frafol.cleanstaffchat.bukkit.donorchat.commands.DonorChatCommand;
import it.frafol.cleanstaffchat.bukkit.general.commands.MuteChatCommand;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl.*;

import java.util.List;

public enum SpigotCommandsConfig {

    STAFFCHAT(StaffChatCommand.class, "aliases.staffchat.main"),
    ADMINCHAT(AdminChatCommand.class, "aliases.adminchat.main"),
    DONORCHAT(DonorChatCommand.class, "aliases.donorchat.main"),

    STAFFLIST(StaffListCommand.class, "aliases.stafflist.main"),

    STAFFCHAT_MUTE(MuteCommand.class, "aliases.staffchat.mute"),
    ADMINCHAT_MUTE(it.frafol.cleanstaffchat.bukkit.adminchat.commands.MuteCommand.class, "aliases.adminchat.mute"),
    DONORCHAT_MUTE(it.frafol.cleanstaffchat.bukkit.donorchat.commands.MuteCommand.class, "aliases.donorchat.mute"),

    STAFFCHAT_TOGGLE(ToggleCommand.class, "aliases.staffchat.toggle"),

    MUTECHAT(MuteChatCommand.class, "aliases.mutechat.main"),

    ADMINCHAT_TOGGLE(it.frafol.cleanstaffchat.bukkit.adminchat.commands.ToggleCommand.class, "aliases.adminchat.toggle"),
    DONORCHAT_TOGGLE(it.frafol.cleanstaffchat.bukkit.donorchat.commands.ToggleCommand.class, "aliases.donorchat.toggle"),

    STAFFCHAT_AFK(AFKCommand.class, "aliases.staffchat.afk");

    private final String path;
    private final Class<?> commandClass;
    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    SpigotCommandsConfig(Class<?> commandClass, String path) {
        this.commandClass = commandClass;
        this.path = path;
    }

    public Class<?> getCommandClass() {
        return commandClass;
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

    public static SpigotCommandsConfig[] getStaffChatCommands() {
        return new SpigotCommandsConfig[]{
                STAFFCHAT,
                STAFFCHAT_MUTE,
                STAFFCHAT_AFK,
                STAFFCHAT_TOGGLE
        };
    }

    public static SpigotCommandsConfig[] getStaffListCommands() {
        return new SpigotCommandsConfig[]{
                STAFFLIST
        };
    }

    public static SpigotCommandsConfig[] getMuteChatCommands() {
        return new SpigotCommandsConfig[]{
                MUTECHAT
        };
    }

    public static SpigotCommandsConfig[] getAdminChatCommands() {
        return new SpigotCommandsConfig[]{
                ADMINCHAT,
                ADMINCHAT_MUTE,
                ADMINCHAT_TOGGLE
        };
    }

    public static SpigotCommandsConfig[] getDonorChatCommands() {
        return new SpigotCommandsConfig[]{
                DONORCHAT,
                DONORCHAT_MUTE,
                DONORCHAT_TOGGLE
        };
    }
}
