package it.frafol.cleanstaffchat.hytale.general.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;

public class ChatListener {

    private final CleanStaffChat plugin;

    public ChatListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    public void onChat(PlayerChatEvent event) {
        PlayerRef player = event.getSender();
        if (PlayerCache.getMutedservers().contains("all")) {
            if (PermissionsUtil.hasPermission(player.getUuid(), HytaleConfig.MUTECHAT_BYPASS_PERMISSION.get(String.class))) return;
            if (event.getContent().startsWith("/")) return;
            player.sendMessage(HytaleMessages.STAFFCHAT_MUTED_ERROR.color()
                    .param("prefix", HytaleMessages.GLOBALPREFIX.color().getRawText()));
            event.setCancelled(true);
        }
    }
}