package it.frafol.cleanstaffchat.hytale.objects;

import com.fancyinnovations.fancycore.api.FancyCore;
import com.fancyinnovations.fancycore.api.permissions.Group;
import com.fancyinnovations.fancycore.api.player.FancyPlayer;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.HytaleServer;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class PermissionsUtil {

    private final CleanStaffChat plugin = CleanStaffChat.getInstance();

    public static String getPrefix(UUID uuid) {
        if (isLuckPerms()) return handleLuckPrefix(uuid);
        if (isFancyCore()) return handleFancyPrefix(uuid);
        return "";
    }

    public static String getSuffix(UUID uuid) {
        if (isLuckPerms()) return handleLuckSuffix(uuid);
        if (isFancyCore()) return handleFancySuffix(uuid);
        return "";
    }

    private static String handleLuckPrefix(UUID uuid) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return "";
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
    }

    private static String handleLuckSuffix(UUID uuid) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return "";
        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix != null ? suffix : "";
    }

    private static String handleFancyPrefix(UUID uuid) {
        FancyCore fancyCore = FancyCore.get();
        FancyPlayer fancyPlayer = fancyCore.getPlayerService().getByUUID(uuid);
        if (fancyPlayer == null) return "";
        Group group = fancyCore.getPermissionService().getGroup(fancyPlayer.getData().getGroups().getFirst());
        if (group == null) return "";
        return group.getPrefix();
    }

    private static String handleFancySuffix(UUID uuid) {
        FancyCore fancyCore = FancyCore.get();
        FancyPlayer fancyPlayer = fancyCore.getPlayerService().getByUUID(uuid);
        if (fancyPlayer == null) return "";
        Group group = fancyCore.getPermissionService().getGroup(fancyPlayer.getData().getGroups().getFirst());
        if (group == null) return "";
        return group.getSuffix();
    }

    private static boolean isLuckPerms() {
        return HytaleServer.get().getPluginManager().hasPlugin(PluginIdentifier.fromString("LuckPerms:LuckPerms"), SemverRange.fromString("*"));
    }

    private static boolean isFancyCore() {
        return HytaleServer.get().getPluginManager().hasPlugin(PluginIdentifier.fromString("FancyInnovations:FancyCore"), SemverRange.fromString("*"));
    }
}
