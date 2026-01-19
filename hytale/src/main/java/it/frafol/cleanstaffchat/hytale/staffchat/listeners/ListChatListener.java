package it.frafol.cleanstaffchat.hytale.staffchat.listeners;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleDiscordConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListChatListener extends ListenerAdapter {

    private final CleanStaffChat plugin;

    public ListChatListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getMessage().getContentDisplay().equalsIgnoreCase("/stafflist")) {
            return;
        }

        if (HytaleDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class).equalsIgnoreCase("none") ||
                !event.getChannel().getId().equalsIgnoreCase(HytaleDiscordConfig.STAFFLIST_CHANNEL_ID.get(String.class))) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        List<UUID> list = new ArrayList<>();

        for (PlayerRef player : Universe.get().getPlayers()) {
            if (!PermissionsUtil.hasPermission(player.getUuid(), HytaleConfig.STAFFLIST_SHOW_PERMISSION.get(String.class))) {
                continue;
            }
            if (Boolean.TRUE.equals(HytaleConfig.STAFFLIST_BYPASS.get(Boolean.class)) &&
                    PermissionsUtil.hasPermission(player.getUuid(), HytaleConfig.STAFFLIST_BYPASS_PERMISSION.get(String.class))) {
                continue;
            }
            list.add(player.getUuid());
        }

        String onlineCount = String.valueOf(list.size());
        String header = HytaleMessages.DISCORDLIST_HEADER.get(String.class).replace("{online}", onlineCount);
        sb.append(header).append("\n");

        if (list.isEmpty()) {
            sb.append(HytaleMessages.DISCORDLIST_NONE.get(String.class)).append("\n");
        } else {
            for (UUID uuid : list) {
                PlayerRef player = Universe.get().getPlayer(uuid);
                if (player == null) continue;

                String isAFK = PlayerCache.getAfk().contains(uuid) ? HytaleMessages.DISCORDLIST_AFK.get(String.class) : "";

                sb.append(HytaleMessages.DISCORDLIST_FORMAT.get(String.class)
                                .replace("{usergroup}", "")
                                .replace("{player}", player.getUsername())
                                .replace("{afk}", isAFK)
                                .replace("{server}", ""))
                        .append("\n");
            }
        }

        String footer = HytaleMessages.DISCORDLIST_FOOTER.get(String.class).replace("{online}", onlineCount);
        sb.append(footer);

        if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(HytaleDiscordConfig.STAFFLIST_EMBED_TITLE.get(String.class));
            embed.setDescription(sb.toString());
            try {
                embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
            } catch (Exception ignored) {}
            embed.setFooter(HytaleDiscordConfig.EMBEDS_FOOTER.get(String.class));
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        } else {
            event.getChannel().sendMessage(sb.toString()).queue();
        }
    }
}