package it.frafol.cleanstaffchat.hytale.staffchat.listeners;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleDiscordConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoveListener {

    private final CleanStaffChat plugin;
    private final Map<UUID, Transform> lastPositions = new HashMap<>();

    public MoveListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    public void update() {
        if (PlayerCache.getAfk().isEmpty()) {
            lastPositions.clear();
            return;
        }

        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            UUID uuid = playerRef.getUuid();
            if (!PlayerCache.getAfk().contains(uuid)) {
                lastPositions.remove(uuid);
                continue;
            }

            Transform currentTransform = playerRef.getTransform();
            Transform lastTransform = lastPositions.get(uuid);

            if (lastTransform != null && !currentTransform.getPosition().equals(lastTransform.getPosition())) {
                lastPositions.remove(uuid);
                handleAfkOff(playerRef);
                continue;
            }

            lastPositions.put(uuid, currentTransform.clone());
        }
    }

    private void handleAfkOff(PlayerRef player) {
        if (!PermissionsModule.get().hasPermission(player.getUuid(), HytaleConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))) {
            return;
        }

        if (!Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_NO_AFK_ONCHANGE_SERVER.get(Boolean.class))) {
            return;
        }

        PlayerCache.getAfk().remove(player.getUuid());

        String staffChatUsePerm = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
        String prefix = HytaleMessages.PREFIX.get(String.class);
        String rawFormat = HytaleMessages.STAFFCHAT_AFK_OFF.get(String.class);

        String finalMessage = rawFormat
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{user}", player.getUsername())
                .replace("{displayname}", player.getUsername())
                .replace("{userprefix}", "")
                .replace("{usersuffix}", "");

        Message hytaleMsg = ChatColor.color((finalMessage));

        Universe.get().getPlayers().stream()
                .filter(p -> PermissionsModule.get().hasPermission(p.getUuid(), staffChatUsePerm)
                        && !PlayerCache.getToggled().contains(p.getUuid()))
                .forEach(p -> p.sendMessage(hytaleMsg));

        sendDiscordAfkOff(player.getUsername());
    }

    private void sendDiscordAfkOff(String username) {
        if (!Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))
                || !Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class))
                || !Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_AFK_MODULE.get(Boolean.class))) {
            return;
        }

        if (plugin.getJda() == null) return;

        TextChannel channel = plugin.getJda().getTextChannelById(HytaleDiscordConfig.STAFF_CHANNEL_ID.get(String.class));
        if (channel == null) return;

        String messageContent = HytaleMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT.get(String.class)
                .replace("{user}", username);

        if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(HytaleDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class));
            embed.setDescription(messageContent);
            try {
                embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
            } catch (Exception ignored) {}
            embed.setFooter(HytaleDiscordConfig.EMBEDS_FOOTER.get(String.class));
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage(messageContent).queue();
        }
    }
}