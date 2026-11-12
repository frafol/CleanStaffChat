package it.frafol.cleanstaffchat.bukkit.objects;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runtime state that is not tied to a chat channel (per-channel state lives in ChatChannel),
 * plus the color helpers. All sets are thread-safe: chat events are fired asynchronously.
 */
@UtilityClass
public class PlayerCache {

    @Getter
    private final Set<UUID> afk = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<UUID> cooldown = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<String> cooldown_discord = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<String> mutedservers = ConcurrentHashMap.newKeySet();

    @Getter
    private final Set<UUID> nochat = ConcurrentHashMap.newKeySet();

    private final Pattern LEGACY_CODE = Pattern.compile("&[0-9a-fk-or]");

    private final Pattern HEX_COLOR = Pattern.compile("(#[A-Fa-f0-9]{6}|<#[A-Fa-f0-9]{6}>|&#[A-Fa-f0-9]{6})");

    public boolean hasColorCodes(@NotNull String message) {
        return LEGACY_CODE.matcher(message).find();
    }

    public String color(String string) {
        final CleanStaffChat instance = CleanStaffChat.getInstance();
        final String converted;
        if (instance.supportsMiniMessage()) {
            final Component component = MiniMessage.miniMessage().deserialize(string);
            converted = LegacyComponentSerializer.legacySection().serialize(component);
        } else {
            converted = convertHexColors(string);
        }
        return converted.replace("&", "§");
    }

    /** Converts #RRGGBB, &amp;#RRGGBB and &lt;#RRGGBB&gt; into the legacy "§x§R§R§G§G§B§B" form. */
    public String convertHexColors(String message) {
        final Matcher matcher = HEX_COLOR.matcher(message);
        final StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            final String hexCode = matcher.group();
            final String digits;
            if (hexCode.startsWith("<#")) {
                digits = hexCode.substring(2, 8);
            } else if (hexCode.startsWith("&#")) {
                digits = hexCode.substring(2, 8);
            } else {
                digits = hexCode.substring(1, 7);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(toLegacyHex(digits)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String toLegacyHex(String digits) {
        final StringBuilder builder = new StringBuilder("§x");
        for (char digit : digits.toCharArray()) {
            builder.append('§').append(digit);
        }
        return builder.toString();
    }
}
