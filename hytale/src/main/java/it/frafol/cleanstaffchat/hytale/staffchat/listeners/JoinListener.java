package it.frafol.cleanstaffchat.hytale.staffchat.listeners;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import it.frafol.cleanstaffchat.hytale.CleanStaffChat;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleDiscordConfig;
import it.frafol.cleanstaffchat.hytale.enums.HytaleMessages;
import it.frafol.cleanstaffchat.hytale.objects.ChatColor;
import it.frafol.cleanstaffchat.hytale.objects.PermissionsUtil;
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class JoinListener {

    public final CleanStaffChat PLUGIN;

    public JoinListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    public void handleJoin(@NotNull PlayerConnectEvent event) {
        final PlayerRef player = event.getPlayerRef();

        if (!Boolean.TRUE.equals(HytaleConfig.STAFF_JOIN_MESSAGE.get(Boolean.class))) {
            return;
        }

        boolean hasStaffPerm = PermissionsModule.get().hasPermission(player.getUuid(), HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class));
        boolean joinAll = Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_JOIN_LEAVE_ALL.get(Boolean.class));

        if (hasStaffPerm || joinAll) {

            if (PermissionsModule.get().hasPermission(player.getUuid(), HytaleConfig.STAFFCHAT_JOIN_SILENT_PERMISSION.get(String.class))
                    && Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_JOIN_SILENT_MODULE.get(Boolean.class))) {
                return;
            }

            String staffChatPerm = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
            String prefix = HytaleMessages.PREFIX.get(String.class);
            String rawFormat = HytaleMessages.STAFF_JOIN_MESSAGE_FORMAT.get(String.class);

            String finalMessage = rawFormat
                    .replace("{prefix}", prefix != null ? prefix : "")
                    .replace("{user}", player.getUsername())
                    .replace("{displayname}", player.getUsername())
                    .replace("{userprefix}", PermissionsUtil.getPrefix(player.getUuid()))
                    .replace("{usersuffix}", PermissionsUtil.getSuffix(player.getUuid()));

            Message hytaleMsg = ChatColor.color((finalMessage));

            Universe.get().getPlayers().stream()
                    .filter(p -> PermissionsModule.get().hasPermission(p.getUuid(), staffChatPerm))
                    .forEach(p -> p.sendMessage(hytaleMsg));

            sendDiscordJoinLeave(player.getUsername(), true);
        }
    }

    public void handleQuit(@NotNull PlayerDisconnectEvent event) {
        final PlayerRef player = event.getPlayerRef();

        PlayerCache.getAfk().remove(player.getUuid());

        if (!Boolean.TRUE.equals(HytaleConfig.STAFF_QUIT_MESSAGE.get(Boolean.class))) {
            return;
        }

        boolean hasStaffPerm = PermissionsModule.get().hasPermission(player.getUuid(), HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class));
        boolean quitAll = Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_QUIT_ALL.get(Boolean.class));

        if (hasStaffPerm || quitAll) {

            if (PermissionsModule.get().hasPermission(player.getUuid(), HytaleConfig.STAFFCHAT_QUIT_SILENT_PERMISSION.get(String.class))
                    && Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_QUIT_SILENT_MODULE.get(Boolean.class))) {
                return;
            }

            String staffChatPerm = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
            String prefix = HytaleMessages.PREFIX.get(String.class);
            String rawFormat = HytaleMessages.STAFF_QUIT_MESSAGE_FORMAT.get(String.class);

            String finalMessage = rawFormat
                    .replace("{prefix}", prefix != null ? prefix : "")
                    .replace("{user}", player.getUsername())
                    .replace("{displayname}", player.getUsername())
                    .replace("{userprefix}", PermissionsUtil.getPrefix(player.getUuid()))
                    .replace("{usersuffix}", PermissionsUtil.getSuffix(player.getUuid()));

            Message hytaleMsg = ChatColor.color((finalMessage));

            Universe.get().getPlayers().stream()
                    .filter(p -> PermissionsModule.get().hasPermission(p.getUuid(), staffChatPerm))
                    .forEach(p -> p.sendMessage(hytaleMsg));

            sendDiscordJoinLeave(player.getUsername(), false);
        }
    }

    private void sendDiscordJoinLeave(String username, boolean join) {
        if (!Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))
                || !Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class))
                || !Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_JOINLEAVE_MODULE.get(Boolean.class))
                || PLUGIN.getJda() == null) {
            return;
        }

        final TextChannel channel = PLUGIN.getJda().getTextChannelById(HytaleDiscordConfig.STAFF_CHANNEL_ID.get(String.class));
        if (channel == null) return;

        String messageFormat = join ? HytaleMessages.STAFF_DISCORD_JOIN_MESSAGE_FORMAT.get(String.class)
                : HytaleMessages.STAFF_DISCORD_QUIT_MESSAGE_FORMAT.get(String.class);

        String description = messageFormat.replace("{user}", username);

        if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(HytaleDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class));
            embed.setDescription(description);
            try {
                embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
            } catch (Exception ignored) {}
            embed.setFooter(HytaleDiscordConfig.EMBEDS_FOOTER.get(String.class));
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage(description).queue();
        }
    }
}