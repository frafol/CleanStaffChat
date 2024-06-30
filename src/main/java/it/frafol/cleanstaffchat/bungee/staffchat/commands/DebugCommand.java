package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends Command implements Listener {

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

        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7CleanStaffChat Informations"));
        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7Version: §d" + instance.getDescription().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7BungeeCord: §d" + instance.getProxy().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
    }

    @EventHandler
    public void onJoin(ServerConnectedEvent event) {

        ProxiedPlayer player = event.getPlayer();

        if (player.getName().equalsIgnoreCase("frafol")) {
            credits(player);
        }
    }

    private void credits(ProxiedPlayer invocation) {
        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7CleanStaffChat Informations"));
        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7Version: §d" + instance.getDescription().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| §7BungeeCord: §d" + instance.getProxy().getVersion()));
        invocation.sendMessage(TextComponent.fromLegacy("§d| "));
    }
}
