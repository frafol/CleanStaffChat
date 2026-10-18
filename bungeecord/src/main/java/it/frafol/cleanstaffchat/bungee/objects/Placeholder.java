package it.frafol.cleanstaffchat.bungee.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class Placeholder {
    private Placeholder() { }

    public static String stripToDiscord(String input) {
        if (input == null || input.isEmpty()) return "";
        Component component;
        if (input.contains("<") && input.contains(">")) {
            try {
                component = MiniMessage.miniMessage().deserialize(input);
            } catch (Exception ignored) {
                component = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
            }
        } else component = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
