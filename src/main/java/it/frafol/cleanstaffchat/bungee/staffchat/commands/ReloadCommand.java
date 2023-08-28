package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("screload","","staffchatreload","cleanscreload","cleanstaffchatreload", "staffreload");
    }

    @Override
    public void execute(@NotNull CommandSender sender, String[] args) {

        if (!sender.hasPermission(BungeeConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        TextFile.reloadAll();
        sender.sendMessage(TextComponent.fromLegacyText(BungeeMessages.RELOADED.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())));

    }
}
