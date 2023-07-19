package it.frafol.cleanstaffchat.bungee.objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class PlayerCache {

    @Getter
    private final HashSet<UUID> toggled = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_2 = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_admin = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_2_admin = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_donor = new HashSet<>();

    @Getter
    private final HashSet<UUID> toggled_2_donor = new HashSet<>();

    @Getter
    private final HashSet<UUID> afk = new HashSet<>();

    @Getter
    private final HashSet<UUID> cooldown = new HashSet<>();

    @Getter
    private final HashSet<String> muted = new HashSet<>();

    @Getter
    private final HashSet<String> muted_admin = new HashSet<>();

    @Getter
    private final HashSet<String> muted_donor = new HashSet<>();

    @Getter
    private final HashSet<String> cooldown_discord = new HashSet<>();

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

    private boolean containsHexColor(String message) {
        String hexColorPattern = "(?i)&#[a-f0-9]{6}";
        return message.matches(".*" + hexColorPattern + ".*");
    }

    public static String translateHex(String message) {

        if (!containsHexColor(message)) {
            return message.replace("&", "§");
        }

        final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
        final char COLOR_CHAR = ChatColor.COLOR_CHAR;

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString().replace("&", "§");
    }
}
