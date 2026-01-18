package it.frafol.cleanstaffchat.hytale.objects;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.HytaleServer;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class LuckPermsUtil {

    private final CleanStaffChat plugin = CleanStaffChat.getInstance();

    public static String getPrefix(UUID uuid) {
        if (!isLuckPerms()) return "";
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return "";
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
    }

    public static String getSuffix(UUID uuid) {
        if (!isLuckPerms()) return "";
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return "";
        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix != null ? suffix : "";
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isLuckPerms() {
        return HytaleServer.get().getPluginManager().hasPlugin(PluginIdentifier.fromString("LuckPerms:LuckPerms"), SemverRange.fromString("*"));
    }
}
