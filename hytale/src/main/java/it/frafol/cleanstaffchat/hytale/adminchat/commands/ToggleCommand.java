package it.frafol.cleanstaffchat.hytale.adminchat.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ToggleCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public ToggleCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
        super(name, description);
        this.plugin = plugin;
        this.setAllowsExtraArguments(true);
        if (aliases != null) {
            this.addAliases(aliases.toArray(String[]::new));
        }
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        String prefix = HytaleMessages.ADMINPREFIX.get(String.class);

        if (!Boolean.TRUE.equals(HytaleConfig.ADMINCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            String disabledMsg = HytaleMessages.MODULE_DISABLED.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((disabledMsg)));
            return CompletableFuture.completedFuture(null);
        }

        if (sender.getUuid() == null) {
            String playerOnly = HytaleMessages.PLAYER_ONLY.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((playerOnly)));
            return CompletableFuture.completedFuture(null);
        }

        if (!PermissionsUtil.hasPermission(sender.getUuid(), HytaleConfig.ADMINCHAT_TOGGLE_PERMISSION.get(String.class))) {
            String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((noPerm)));
            return CompletableFuture.completedFuture(null);
        }

        if (!PlayerCache.getToggled_admin().contains(sender.getUuid())) {
            PlayerCache.getToggled_admin().add(sender.getUuid());
            String toggledOff = HytaleMessages.ADMINCHAT_TOGGLED_OFF.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((toggledOff)));
        } else {
            PlayerCache.getToggled_admin().remove(sender.getUuid());
            String toggledOn = HytaleMessages.ADMINCHAT_TOGGLED_ON.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((toggledOn)));
        }

        return CompletableFuture.completedFuture(null);
    }
}