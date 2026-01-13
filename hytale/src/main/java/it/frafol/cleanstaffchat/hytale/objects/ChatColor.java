package it.frafol.cleanstaffchat.hytale.objects;

import com.hypixel.hytale.server.core.Message;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;

public class ChatColor {

    public static Message color(String string) {
        string = string.replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
        return translate(string);
    }

    public static Message translate(String input) {
        // TODO: Message colors
        return Message.raw(input);
    }
}
