package it.frafol.cleanstaffchat.hytale.general.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClearChatCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public ClearChatCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
        super(name, description);
        this.plugin = plugin;
        this.setAllowsExtraArguments(true);
        this.requirePermission(Objects.requireNonNull(HytaleConfig.CLEARCHAT_PERMISSION.get(String.class)));
        if (aliases != null) {
            this.addAliases(aliases.toArray(new String[0]));
        }
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        String senderName = sender.getDisplayName();
        Universe.get().getPlayers().forEach(player -> {
            for (int i = 0; i < 100; i++) player.sendMessage(Message.raw(""));
        });
        broadcastClearChat(sender.getUuid(), senderName);
        return CompletableFuture.completedFuture(null);
    }

    private void broadcastClearChat(UUID uuid, String senderName) {
        String clearchat = HytaleMessages.CLEANED.get(String.class)
                .replace("{user}", senderName)
                .replace("{userprefix}", PermissionsUtil.getPrefix(uuid))
                .replace("{usersuffix}", PermissionsUtil.getSuffix(uuid));
        Universe.get().getPlayers().forEach(player -> player.sendMessage(ChatColor.color(clearchat)));
    }
}