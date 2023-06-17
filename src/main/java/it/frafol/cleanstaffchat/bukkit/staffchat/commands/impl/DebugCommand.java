package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class DebugCommand implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();

        if (!event.getMessage().startsWith("/")) {
            return;
        }

        if (!(event.getMessage().equals("/scdebug") || event.getMessage().equals("/staffchatdebug") || event.getMessage().equals("/staffdebug"))) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage("§d| ");
        player.sendMessage("§d| §7CleanScreenShare Informations");
        player.sendMessage("§d| ");
        player.sendMessage("§d| §7Version: §d" + CleanStaffChat.getInstance().getDescription().getVersion());
        player.sendMessage("§d| §7BungeeCord: §d" + CleanStaffChat.getInstance().getServer().getVersion());
        player.sendMessage("§d| ");

    }
}
