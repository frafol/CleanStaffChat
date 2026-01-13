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

public class MuteCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public MuteCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
        super(name, description);
        this.plugin = plugin;
        this.setAllowsExtraArguments(true);
        if (aliases != null) {
            this.addAliases(aliases.toArray(new String[0]));
        }
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (Boolean.FALSE.equals(HytaleConfig.STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
            String disabledMsg = HytaleMessages.MODULE_DISABLED.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((disabledMsg)));
            return CompletableFuture.completedFuture(null);
        }

        String permission = HytaleConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class);
        if (!PermissionsModule.get().hasPermission(sender.getUuid(), permission)) {
            String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((noPerm)));
            return CompletableFuture.completedFuture(null);
        }

        String prefix = HytaleMessages.PREFIX.get(String.class);

        if (!PlayerCache.getMuted().contains("true")) {
            PlayerCache.getMuted().add("true");
            String mutedMsg = HytaleMessages.STAFFCHAT_MUTED.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((mutedMsg)));
        } else {
            PlayerCache.getMuted().remove("true");
            String unmutedMsg = HytaleMessages.STAFFCHAT_UNMUTED.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((unmutedMsg)));
        }

        return CompletableFuture.completedFuture(null);
    }
}