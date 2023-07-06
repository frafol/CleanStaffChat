package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends Command {

    public final CleanStaffChat instance;

    public DebugCommand(CleanStaffChat instance) {
        super("scdebug","","staffchatdebug","cleanscdebug","cleanstaffchatdebug", "staffdebug");
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull CommandSender invocation, String[] args) {

        if (args.length != 0) {
            return;
        }

        invocation.sendMessage(TextComponent.fromLegacyText("§d| "));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| §7CleanStaffChat Informations"));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| "));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| §7Version: §d" + instance.getDescription().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| §7BungeeCord: §d" + instance.getProxy().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacyText("§d| "));

    }
}
