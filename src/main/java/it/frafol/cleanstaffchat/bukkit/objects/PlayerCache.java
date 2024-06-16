package it.frafol.cleanstaffchat.bukkit.objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;
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

    @Getter
    private final HashSet<String> mutedservers = new HashSet<>();

    @Getter
    private final HashSet<UUID> nochat = new HashSet<>();

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

    public String color(String string) {
        String hex = convertHexColors(string);
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
        String[] hexColorPattern = new String[]{"#[a-fA-F0-9]{6}", "&#[a-fA-F0-9]{6}", "<#[a-fA-F0-9]]{6}>"};
        for (String pattern : hexColorPattern) {
            if (Pattern.compile(pattern).matcher(message).find()) {
                return true;
            }
        }
        return false;
    }
}
