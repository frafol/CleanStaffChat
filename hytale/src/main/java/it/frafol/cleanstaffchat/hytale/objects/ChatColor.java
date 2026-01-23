package it.frafol.cleanstaffchat.hytale.objects;

import com.hypixel.hytale.server.core.Message;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColor {

    private static final Map<Character, Color> COLOR_MAP = new HashMap<>();
    private static final Pattern PATTERN = Pattern.compile(
            "[&§]([0-9a-fk-or])|" +
                    "[&§]#([A-Fa-f0-9]{6})|" +
                    "<#([A-Fa-f0-9]{6})>|" +
                    "</#([A-Fa-f0-9]{6})>|" +
                    "<(bold|italic|reset|b|i|/bold|/italic|/b|/i)>"
    );

    static {
        COLOR_MAP.put('0', new Color(0x000000));
        COLOR_MAP.put('1', new Color(0x0000AA));
        COLOR_MAP.put('2', new Color(0x00AA00));
        COLOR_MAP.put('3', new Color(0x00AAAA));
        COLOR_MAP.put('4', new Color(0xAA0000));
        COLOR_MAP.put('5', new Color(0xAA00AA));
        COLOR_MAP.put('6', new Color(0xFFAA00));
        COLOR_MAP.put('7', new Color(0xAAAAAA));
        COLOR_MAP.put('8', new Color(0x555555));
        COLOR_MAP.put('9', new Color(0x5555FF));
        COLOR_MAP.put('a', new Color(0x55FF55));
        COLOR_MAP.put('b', new Color(0x55FFFF));
        COLOR_MAP.put('c', new Color(0xFF5555));
        COLOR_MAP.put('d', new Color(0xFF55FF));
        COLOR_MAP.put('e', new Color(0xFFFF55));
        COLOR_MAP.put('f', new Color(0xFFFFFF));
    }

    public static Message color(String string) {
        if (string == null) return Message.raw("");
        string = string.replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
        return translate(string);
    }

    public static Message translate(String input) {
        return format(input);
    }

    private static Message format(String text) {
        if (text == null || text.isEmpty()) return Message.raw("");

        List<Message> messages = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(text);
        int lastIndex = 0;

        Color currentColor = Color.WHITE;
        boolean bold = false;
        boolean italic = false;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) messages.add(applyStyles(text.substring(lastIndex, matcher.start()), currentColor, bold, italic));
            if (matcher.group(1) != null) {
                char code = matcher.group(1).charAt(0);
                if (COLOR_MAP.containsKey(code)) {
                    currentColor = COLOR_MAP.get(code);
                } else if (code == 'r') {
                    currentColor = Color.WHITE; bold = false; italic = false;
                } else if (code == 'l') bold = true;
                else if (code == 'o') italic = true;
            }

            else if (matcher.group(2) != null || matcher.group(3) != null) {
                String hex = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                currentColor = new Color(Integer.parseInt(hex, 16));
            }

            else if (matcher.group(4) != null) {
                currentColor = Color.WHITE;
            }

            else if (matcher.group(5) != null) {
                String tag = matcher.group(5).toLowerCase();
                switch (tag) {
                    case "bold": case "b": bold = true; break;
                    case "/bold": case "/b": bold = false; break;
                    case "italic": case "i": italic = true; break;
                    case "/italic": case "/i": italic = false; break;
                    case "reset": currentColor = Color.WHITE; bold = false; italic = false; break;
                }
            }
            lastIndex = matcher.end();
        }

        if (lastIndex < text.length()) messages.add(applyStyles(text.substring(lastIndex), currentColor, bold, italic));
        return messages.isEmpty() ? Message.raw("") : Message.join(messages.toArray(new Message[0]));
    }

    private static Message applyStyles(String content, Color color, boolean bold, boolean italic) {
        Message msg = Message.raw(content).color(color);
        if (bold) msg = msg.bold(true);
        if (italic) msg = msg.italic(true);
        return msg;
    }
}