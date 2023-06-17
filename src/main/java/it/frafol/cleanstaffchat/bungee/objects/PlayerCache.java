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
            return message;
        }

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
