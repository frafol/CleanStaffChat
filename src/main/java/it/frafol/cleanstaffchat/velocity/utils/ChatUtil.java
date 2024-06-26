package it.frafol.cleanstaffchat.velocity.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import io.github.miniplaceholders.api.MiniPlaceholders;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ChatUtil {
    private static final CleanStaffChat instance = CleanStaffChat.getInstance();

    public String getString(@NotNull VelocityMessages velocityMessages) {
        return instance.getMessagesTextFile().getConfig().getString(velocityMessages.getPath());
    }

    public String getString(VelocityMessages velocityMessages, Placeholder... placeholders) {
        return applyPlaceholder(getString(velocityMessages), placeholders);
    }

    public String getFormattedString(VelocityMessages velocityMessages, Placeholder... placeholders) {
        return color(getString(velocityMessages, placeholders));
    }

    public String getFormattedString(Player player, VelocityMessages velocityMessages, Placeholder... placeholders) {
        return color(player, getString(velocityMessages, placeholders));
    }

    public String applyPlaceholder(String s, Placeholder @NotNull ... placeholders) {
        for (Placeholder placeholder : placeholders) {
            s = s.replace(placeholder.getKey(), placeholder.getValue());
        }

        return s;
    }

    public String color(@NotNull String s) {
        return convertHexColors(s).replace("&", "§");
    }

    public String color(@NotNull Player p, @NotNull String s) {
        if (instance.getMiniPlaceholders()) {
            TagResolver resolver = MiniPlaceholders.getAudiencePlaceholders(p);
            Component parsedMessage = MiniMessage.miniMessage().deserialize(s, resolver);
            s = LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
            TagResolver globalResolver = MiniPlaceholders.getGlobalPlaceholders();
            Component parsedGlobalMessage = MiniMessage.miniMessage().deserialize(s, globalResolver);
            s = LegacyComponentSerializer.legacyAmpersand().serialize(parsedGlobalMessage);
        }
        return convertHexColors(s).replace("&", "§");
    }

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

    public String translateHex(String string) {
        return convertHexColors(string);
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

    @SuppressWarnings("UnstableApiUsage")
    public void sendChannelMessage(Player player, boolean cancel) {
        if (!VelocityConfig.DOUBLE_MESSAGE.get(Boolean.class)) {
            return;
        }
        final ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(String.valueOf(cancel));
        buf.writeUTF(player.getUsername());
        player.getCurrentServer().ifPresent(sv ->
                sv.sendPluginMessage(CleanStaffChat.channel, buf.toByteArray()));
    }
}
