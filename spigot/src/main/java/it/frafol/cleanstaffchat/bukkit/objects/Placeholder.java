package it.frafol.cleanstaffchat.bukkit.objects;

import org.bukkit.ChatColor;

import java.util.regex.Pattern;

public final class Placeholder {
    private Placeholder() { }

    private static final Pattern MINIMESSAGE_PATTERN = Pattern.compile("<[^>]+>");

    public static String stripToDiscord(String input) {
        if (input == null || input.isEmpty()) return "";
        String processed = input;
        if (processed.contains("<") && processed.contains(">")) {
            processed = MINIMESSAGE_PATTERN.matcher(processed).replaceAll("");
        }
        processed = ChatColor.translateAlternateColorCodes('&', processed);
        return ChatColor.stripColor(processed);
    }
}
