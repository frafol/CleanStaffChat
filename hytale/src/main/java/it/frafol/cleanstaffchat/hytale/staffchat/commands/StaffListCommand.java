package it.frafol.cleanstaffchat.hytale.staffchat.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaffListCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public StaffListCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
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

        String[] args = context.getInputString().split(" ");
        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        String listPermission = HytaleConfig.STAFFLIST_PERMISSION.get(String.class);
        String showPermission = HytaleConfig.STAFFLIST_SHOW_PERMISSION.get(String.class);
        String prefix = HytaleMessages.PREFIX.get(String.class);

        if (!PermissionsUtil.hasPermission(sender.getUuid(), listPermission)) {
            String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((noPerm)));
            return CompletableFuture.completedFuture(null);
        }

        if (args.length != 0) {
            String usage = HytaleMessages.LIST_USAGE.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((usage)));
            return CompletableFuture.completedFuture(null);
        }

        List<PlayerRef> staffOnline = new ArrayList<>();
        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                if (PermissionsUtil.hasPermission(ref.getUuid(), showPermission)) {
                    staffOnline.add(ref);
                }
            }
        });

        String onlineCount = String.valueOf(staffOnline.size());

        String header = HytaleMessages.LIST_HEADER.get(String.class)
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{online}", onlineCount);
        sender.sendMessage(ChatColor.color((header)));

        if (staffOnline.isEmpty()) {
            String none = HytaleMessages.LIST_NONE.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((none)));
        } else {
            for (PlayerRef staff : staffOnline) {
                String isAFK = PlayerCache.getAfk().contains(staff.getUuid())
                        ? HytaleMessages.STAFFLIST_AFK.get(String.class).replace("&", "§")
                        : "";

                String format = HytaleMessages.LIST_FORMAT.get(String.class)
                        .replace("{prefix}", prefix != null ? prefix : "")
                        .replace("{userprefix}", PermissionsUtil.getPrefix(sender.getUuid()))
                        .replace("{usersuffix}", PermissionsUtil.getSuffix(sender.getUuid()))
                        .replace("{player}", staff.getUsername())
                        .replace("{afk}", isAFK)
                        .replace("{server}", "");

                sender.sendMessage(ChatColor.color((format)));
            }
        }

        String footer = HytaleMessages.LIST_FOOTER.get(String.class)
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{online}", onlineCount);
        sender.sendMessage(ChatColor.color((footer)));

        return CompletableFuture.completedFuture(null);
    }
}