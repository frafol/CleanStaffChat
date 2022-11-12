package it.frafol.cleanstaffchat.bukkit.staffchat.commands;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class CommandBase extends Command implements PluginIdentifiableCommand {

    protected final CleanStaffChat plugin;

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args);

    protected CommandBase(CleanStaffChat plugin, String name, String usageMessage, List<String> aliases) {
        super(name, "", usageMessage, aliases);
        this.plugin = plugin;
    }

    protected boolean inGameCheck(CommandSender sender) {
       return inGameCheck(sender, (SpigotMessages.PLAYER_ONLY.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color())));
    }

    protected boolean inGameCheck(CommandSender sender, String message) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(message);
            return false;
        }
        return true;
    }

    @Override
    public CleanStaffChat getPlugin() {
        return plugin;
    }

}
