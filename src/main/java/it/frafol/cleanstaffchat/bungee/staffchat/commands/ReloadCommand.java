package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
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

    private final CleanStaffChat plugin = CleanStaffChat.getInstance();

    @Override
    public void execute(@NotNull CommandSender sender, String[] args) {

        if (!sender.hasPermission(BungeeConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        TextFile.reloadAll();
        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.RELOADED.color()
                .replace("%prefix%", BungeeMessages.PREFIX.color())));

        if (plugin.getJda() == null && BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            plugin.startJDA();
        }
    }
}
