package it.frafol.cleanstaffchat.utils;

import com.velocitypowered.api.command.CommandSource;
import it.frafol.cleanstaffchat.CleanStaffChat;
import it.frafol.cleanstaffchat.objects.Placeholder;
import it.frafol.cleanstaffchat.enums.VelocityConfig;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<String> getStringList(VelocityConfig velocityMessages) {
        return instance.getConfigTextFile().getConfig().getStringList(velocityMessages.getPath());
    }

    public List<String> getStringList(VelocityConfig velocityMessages, Placeholder... placeholders) {
        List<String> newList = new ArrayList<>();

        for (String s : getStringList(velocityMessages)) {
            s = applyPlaceholder(s, placeholders);
            newList.add(s);
        }

        return newList;
    }

    public String applyPlaceholder(String s, Placeholder... placeHolders) {
        for (Placeholder placeHolder : placeHolders) {
            s = s.replace(placeHolder.getKey(), placeHolder.getValue());
        }

        return s;
    }

    public String color(String s) {

        return s.replace("&", "§");

    }

    public List<String> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public void sendList(CommandSource commandSource, List<String> stringList) {
        for (String message : stringList) {
            commandSource.sendMessage(Component.text(message));
        }
    }

    public void sendFormattedList(VelocityConfig velocityMessages, CommandSource commandSource, Placeholder... placeholders) {
        sendList(commandSource, color(getStringList(velocityMessages, placeholders)));
    }
}
