package it.frafol.cleanstaffchat.hytale.adminchat.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MuteCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public MuteCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
        super(name, description);
        this.plugin = plugin;
        this.setAllowsExtraArguments(true);
        this.requirePermission(Objects.requireNonNull(HytaleConfig.ADMINCHAT_MUTE_PERMISSION.get(String.class)));
        if (aliases != null) {
            this.addAliases(aliases.toArray(String[]::new));
        }
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        String prefix = HytaleMessages.ADMINPREFIX.get(String.class);

        if (!Boolean.TRUE.equals(HytaleConfig.ADMINCHAT_MUTE_MODULE.get(Boolean.class))) {
            String disabledMsg = HytaleMessages.MODULE_DISABLED.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((disabledMsg)));
            return CompletableFuture.completedFuture(null);
        }

        if (!PermissionsUtil.hasPermission(sender.getUuid(), HytaleConfig.ADMINCHAT_MUTE_PERMISSION.get(String.class))) {
            String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((noPerm)));
            return CompletableFuture.completedFuture(null);
        }

        if (!PlayerCache.getMuted().contains("true")) {
            PlayerCache.getMuted().add("true");
            String mutedMsg = HytaleMessages.ADMINCHAT_MUTED.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((mutedMsg)));
        } else {
            PlayerCache.getMuted().remove("true");
            String unmutedMsg = HytaleMessages.ADMINCHAT_UNMUTED.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((unmutedMsg)));
        }

        return CompletableFuture.completedFuture(null);
    }
}