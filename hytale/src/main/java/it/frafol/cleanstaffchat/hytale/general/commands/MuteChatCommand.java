package it.frafol.cleanstaffchat.hytale.general.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class MuteChatCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public MuteChatCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
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
        String input = context.getInputString().trim();
        String[] split = input.split("\\s+");

        if (split.length == 1) {
            if (!PermissionsUtil.hasPermission(sender.getUuid(), HytaleConfig.MUTECHAT_ALL_PERMISSION.get(String.class)) &&
                    !PermissionsUtil.hasPermission(sender.getUuid(), HytaleConfig.MUTECHAT_PERMISSION.get(String.class))) {
                sender.sendMessage(HytaleMessages.NO_PERMISSION.color()
                        .param("prefix", HytaleMessages.GLOBALPREFIX.color().getRawText()));
                return CompletableFuture.completedFuture(null);
            }

            String senderName = sender.getDisplayName();

            if (PlayerCache.getMutedservers().contains("all")) {
                PlayerCache.getMutedservers().remove("all");
                broadcastMuteChat(senderName, false);
                sender.sendMessage(HytaleMessages.MUTECHAT_DISABLED.color()
                        .param("prefix", HytaleMessages.GLOBALPREFIX.color().getRawText())
                        .param("user", senderName)
                        .param("userprefix", "")
                        .param("usersuffix", ""));
                return CompletableFuture.completedFuture(null);
            }

            PlayerCache.getMutedservers().add("all");
            broadcastMuteChat(senderName, true);
            sender.sendMessage(HytaleMessages.MUTECHAT_ENABLED.color()
                    .param("prefix", HytaleMessages.GLOBALPREFIX.color().getRawText())
                    .param("userprefix", "")
                    .param("user", senderName)
                    .param("usersuffix", ""));
        } else {
            sender.sendMessage(HytaleMessages.MUTECHAT_USAGE.color()
                    .param("prefix", HytaleMessages.GLOBALPREFIX.color().getRawText()));
        }
        return CompletableFuture.completedFuture(null);
    }

    private void broadcastMuteChat(String senderName, boolean activated) {
        Stream<PlayerRef> targetPlayers;
        if (Boolean.TRUE.equals(HytaleConfig.MUTECHAT_BC_ALL.get(Boolean.class))) {
            targetPlayers = Universe.get().getPlayers().stream();
        } else {
            String staffPerm = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
            targetPlayers = Universe.get().getPlayers().stream()
                    .filter(player -> PermissionsUtil.hasPermission(player.getUuid(), staffPerm)
                            && !(PlayerCache.getToggled().contains(player.getUuid())));
        }

        String prefix = HytaleMessages.GLOBALPREFIX.color().getRawText();

        if (activated) {
            targetPlayers.forEach(player -> player.sendMessage(HytaleMessages.MUTECHAT_ENABLED_BC.color()
                    .param("prefix", prefix)
                    .param("userprefix", "")
                    .param("user", senderName)
                    .param("usersuffix", "")));
            return;
        }

        targetPlayers.forEach(player -> player.sendMessage(HytaleMessages.MUTECHAT_DISABLED_BC.color()
                .param("prefix", prefix)
                .param("userprefix", "")
                .param("user", senderName)
                .param("usersuffix", "")));
    }
}