package it.frafol.cleanstaffchat.bungee.objects;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runtime state that is not tied to a chat channel (per-channel state lives in ChatChannel),
 * plus the color helpers. All sets are thread-safe: chat events and Discord callbacks run on different threads.
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

    private final Pattern LEGACY_CODE = Pattern.compile("&[0-9a-fk-or]");

    private final Pattern HEX_COLOR = Pattern.compile("(#[A-Fa-f0-9]{6}|<#[A-Fa-f0-9]{6}>|&#[A-Fa-f0-9]{6})");

    public boolean hasColorCodes(@NotNull String message) {
        return LEGACY_CODE.matcher(message).find();
    }

    public String translateHex(String string) {
        return convertHexColors(string).replace("&", "§");
    }

    private String convertHexColors(String message) {
        if (BungeeConfig.MINIMESSAGE.get(Boolean.class)) return formatMiniMessage(message);

        final Matcher matcher = HEX_COLOR.matcher(message);
        final StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            final String hexCode = matcher.group();
            final String digits = hexCode.startsWith("<#") || hexCode.startsWith("&#")
                    ? hexCode.substring(2, 8) : hexCode.substring(1, 7);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(toLegacyHex(digits)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public String formatMiniMessage(String input) {
        final Component raw = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
        final String intermediate = MiniMessage.miniMessage().serialize(raw)
                .replace("§", "")
                .replace("&", "");
        final Component parsed = MiniMessage.miniMessage().deserialize(intermediate);
        return LegacyComponentSerializer.legacyAmpersand().serialize(parsed);
    }

    private String toLegacyHex(String digits) {
        final StringBuilder builder = new StringBuilder("§x");
        for (char digit : digits.toCharArray()) {
            builder.append('§').append(digit);
        }
        return builder.toString();
    }

    /** Tells the backend server (kick workaround) whether it must cancel the player's chat messages. */
    public void sendChannelMessage(ProxiedPlayer player, boolean cancel) {
        if (!BungeeConfig.WORKAROUND_KICK.get(Boolean.class)) return;
        if (player.getServer() == null) return;
        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(String.valueOf(cancel));
        buf.writeUTF(player.getName());
        player.getServer().sendData("cleansc:cancel", buf.toByteArray());
    }
}
