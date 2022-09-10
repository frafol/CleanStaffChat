package it.frafol.cleanstaffchat.bungee.Commands;

import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.TextFile;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("screload","","staffchatreload","cleanscreload","cleanstaffchatreload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission(BungeeConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            TextFile.reloadAll();
            sender.sendMessage(new TextComponent(BungeeConfig.RELOADED.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                    .replace("&", "§")));
        } else {
            sender.sendMessage(new TextComponent(BungeeConfig.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeConfig.PREFIX.color())
                    .replace("&", "§")));
        }
    }
}
