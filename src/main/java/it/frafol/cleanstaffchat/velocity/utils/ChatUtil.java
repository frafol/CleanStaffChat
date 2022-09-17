package it.frafol.cleanstaffchat.velocity.utils;

import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ChatUtil {
    private static final CleanStaffChat instance = CleanStaffChat.getInstance();

    public String getString(VelocityConfig velocityMessages) {
        return instance.getConfigTextFile().getConfig().getString(velocityMessages.getPath());
    }

    public String getString(VelocityConfig velocityMessages, Placeholder... placeholders) {
        return applyPlaceholder(getString(velocityMessages), placeholders);
    }

    public String getFormattedString(VelocityConfig velocityMessages, Placeholder... placeholders) {
        return color(getString(velocityMessages, placeholders));
    }

    public String applyPlaceholder(String s, Placeholder... placeholders) {
        for (Placeholder placeholder : placeholders) {
            s = s.replace(placeholder.getKey(), placeholder.getValue());
        }

        return s;
    }

    public String color(String s) {

        return s.replace("&", "§");

    }
}
