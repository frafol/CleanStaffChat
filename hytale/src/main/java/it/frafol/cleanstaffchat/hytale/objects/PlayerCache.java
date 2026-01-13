package it.frafol.cleanstaffchat.hytale.objects;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

public class PlayerCache {

    private static final HashSet<UUID> toggled = new HashSet<>();
    private static final HashSet<UUID> toggled_2 = new HashSet<>();
    private static final HashSet<UUID> toggled_admin = new HashSet<>();
    private static final HashSet<UUID> toggled_2_admin = new HashSet<>();
    private static final HashSet<UUID> toggled_donor = new HashSet<>();
    private static final HashSet<UUID> toggled_2_donor = new HashSet<>();
    private static final HashSet<UUID> afk = new HashSet<>();
    private static final HashSet<UUID> cooldown = new HashSet<>();
    private static final HashSet<String> muted = new HashSet<>();
    private static final HashSet<String> muted_admin = new HashSet<>();
    private static final HashSet<String> muted_donor = new HashSet<>();
    private static final HashSet<String> cooldown_discord = new HashSet<>();
    private static final HashSet<String> mutedservers = new HashSet<>();
    public static HashSet<UUID> getToggled() { return toggled; }
    public static HashSet<UUID> getToggled_2() { return toggled_2; }
    public static HashSet<UUID> getToggled_admin() { return toggled_admin; }
    public static HashSet<UUID> getToggled_2_admin() { return toggled_2_admin; }
    public static HashSet<UUID> getToggled_donor() { return toggled_donor; }
    public static HashSet<UUID> getToggled_2_donor() { return toggled_2_donor; }
    public static HashSet<UUID> getAfk() { return afk; }
    public static HashSet<UUID> getCooldown() { return cooldown; }
    public static HashSet<String> getMuted() { return muted; }
    public static HashSet<String> getMuted_admin() { return muted_admin; }
    public static HashSet<String> getMuted_donor() { return muted_donor; }
    public static HashSet<String> getCooldown_discord() { return cooldown_discord; }
    public static HashSet<String> getMutedservers() { return mutedservers; }

    public boolean hasColorCodes(@NotNull String message) {
        return message.contains("&0") ||
                message.contains("&1") ||
                message.contains("&2") ||
                message.contains("&3") ||
                message.contains("&4") ||
                message.contains("&5") ||
                message.contains("&6") ||
                message.contains("&7") ||
                message.contains("&8") ||
                message.contains("&9") ||
                message.contains("&a") ||
                message.contains("&b") ||
                message.contains("&c") ||
                message.contains("&d") ||
                message.contains("&e") ||
                message.contains("&f") ||
                message.contains("&k") ||
                message.contains("&l") ||
                message.contains("&m") ||
                message.contains("&n") ||
                message.contains("&o") ||
                message.contains("&r");
    }
}
