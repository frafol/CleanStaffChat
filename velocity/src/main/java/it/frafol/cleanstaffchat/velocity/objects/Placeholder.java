package it.frafol.cleanstaffchat.velocity.objects;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;


@Getter
public class Placeholder { private final String key;private final String value;
    public Placeholder(String key, String value) {this.key = "%" + key + "%";this.value = value;
    }

    public static String stripToDiscord(String input) {
        if (input == null || input.isEmpty()) return "";
        Component component;
        if (input.contains("<") && input.contains(">")) {
            try {
                component = MiniMessage.miniMessage().deserialize(input);
            } catch (Exception e) {
                component = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
            }
        } else component = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}