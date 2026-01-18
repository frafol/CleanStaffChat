package it.frafol.cleanstaffchat.hytale.staffchat.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleDiscordConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.TextFile;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ReloadCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public ReloadCommand(CleanStaffChat plugin) {
        super("screload", "Ricarica la configurazione del plugin");
        this.plugin = plugin;
        this.addAliases("staffchatreload", "cleanscreload", "cleanstaffchatreload", "staffreload");
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String permission = HytaleConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class);

        if (!PermissionsUtil.hasPermission(context.sender().getUuid(), permission)) {
            String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            context.sender().sendMessage(ChatColor.color((noPerm)));
            return CompletableFuture.completedFuture(null);
        }

        TextFile.reloadAll();

        String reloadedMsg = HytaleMessages.RELOADED.get(String.class)
                .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
        context.sender().sendMessage(ChatColor.color((reloadedMsg)));

        if (plugin.getJda() == null && Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))) {
            plugin.startJDA();
        }

        return CompletableFuture.completedFuture(null);
    }
}