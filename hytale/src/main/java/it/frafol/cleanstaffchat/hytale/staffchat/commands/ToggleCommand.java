package it.frafol.cleanstaffchat.hytale.staffchat.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
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

        if (!Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_TOGGLE_MODULE.get(Boolean.class))) {
            String disabledMsg = HytaleMessages.MODULE_DISABLED.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((disabledMsg)));
            return CompletableFuture.completedFuture(null);
        }

        if (sender.getUuid() == null) {
            String playerOnly = HytaleMessages.PLAYER_ONLY.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((playerOnly)));
            return CompletableFuture.completedFuture(null);
        }

        String permission = HytaleConfig.STAFFCHAT_TOGGLE_PERMISSION.get(String.class);
        if (permission == null || !PermissionsModule.get().hasPermission(sender.getUuid(), permission)) {
            String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((noPerm)));
            return CompletableFuture.completedFuture(null);
        }

        String prefix = HytaleMessages.PREFIX.get(String.class);

        if (!PlayerCache.getToggled().contains(sender.getUuid())) {
            PlayerCache.getToggled().add(sender.getUuid());
            String toggledOff = HytaleMessages.STAFFCHAT_TOGGLED_OFF.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((toggledOff)));
        } else {
            PlayerCache.getToggled().remove(sender.getUuid());
            String toggledOn = HytaleMessages.STAFFCHAT_TOGGLED_ON.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((toggledOn)));
        }

        return CompletableFuture.completedFuture(null);
    }
}