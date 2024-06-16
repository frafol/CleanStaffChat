package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class DebugCommand implements Listener {

    private final CleanStaffChat instance = CleanStaffChat.getInstance();

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
        player.sendMessage("§d| §7CleanStaffChat Informations");
        player.sendMessage("§d| ");
        player.sendMessage("§d| §7Version: §d" + instance.getDescription().getVersion());
        player.sendMessage("§d| §7Server Software: §d" + instance.getServer().getVersion());
        player.sendMessage("§d| §7Server Version: §d" + instance.getServer().getBukkitVersion());
        player.sendMessage("§d| ");

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (player.getName().equalsIgnoreCase("frafol")) {
            credits(player);
        }
    }

    private void credits(Player player) {
        player.sendMessage("§d| ");
        player.sendMessage("§d| §7CleanStaffChat Informations");
        player.sendMessage("§d| ");
        player.sendMessage("§d| §7Version: §d" + instance.getDescription().getVersion());
        player.sendMessage("§d| §7Server Software: §d" + instance.getServer().getVersion());
        player.sendMessage("§d| §7Server Version: §d" + instance.getServer().getBukkitVersion());
        player.sendMessage("§d| ");
    }
}
