package it.frafol.cleanstaffchat.bukkit.staffchat.commands.impl;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.TextFile;
import it.frafol.cleanstaffchat.bukkit.staffchat.commands.CommandBase;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ReloadCommand extends CommandBase {

    public ReloadCommand(CleanStaffChat plugin) {
        super(plugin, "screload", "/screload", Arrays.asList("staffchatreload", "cleanscreload", "cleanstaffchatreload", "staffreload"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if (!sender.hasPermission(SpigotConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            sender.sendMessage(SpigotMessages.NO_PERMISSION.color()
                    .replace("%prefix%", SpigotMessages.PREFIX.color()));
            return false;
        }

        TextFile.reloadAll();
        sender.sendMessage(SpigotMessages.RELOADED.color()
                .replace("%prefix%", SpigotMessages.PREFIX.color()));

        if (plugin.getJda() == null && SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            plugin.startJDA();
        }

        return false;
    }
}
