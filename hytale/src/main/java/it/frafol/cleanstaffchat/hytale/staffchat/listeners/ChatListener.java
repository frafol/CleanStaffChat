package it.frafol.cleanstaffchat.hytale.staffchat.listeners;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class ChatListener extends ListenerAdapter {

    private final CleanStaffChat plugin;

    public ChatListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    public void onChat(@NotNull PlayerChatEvent event) {
        PlayerRef player = event.getSender();
        String rawMessage = event.getContent();
        String staffChatPerm = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
        String scPrefix = HytaleConfig.STAFFCHAT_PREFIX.get(String.class);

        if (Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_PREFIX_MODULE.get(Boolean.class)) && rawMessage.startsWith(scPrefix)) {
            if (!PermissionsUtil.hasPermission(player.getUuid(), staffChatPerm)) return;
            event.setCancelled(true);
            String message = rawMessage.substring(scPrefix.length());
            broadcastToStaff(player, message);
            sendToDiscord(player, message);
            return;
        }

        if (PlayerCache.getToggled_2().contains(player.getUuid())) {
            if (PlayerCache.getMuted().contains("true")) {
                PlayerCache.getToggled_2().remove(player.getUuid());
                event.setCancelled(true);
                String mutedError = HytaleMessages.STAFFCHAT_MUTED_ERROR.get(String.class)
                        .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
                player.sendMessage(ChatColor.color((mutedError)));
                return;
            }

            if (rawMessage.startsWith("/")) return;

            if (!Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_TALK_MODULE.get(Boolean.class))) {
                String disabledMsg = HytaleMessages.MODULE_DISABLED.get(String.class)
                        .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));

                player.sendMessage(ChatColor.color((disabledMsg)));
                return;
            }

            if (PermissionsUtil.hasPermission(player.getUuid(), staffChatPerm)) {
                event.setCancelled(true);
                broadcastToStaff(player, rawMessage);
                sendToDiscord(player, rawMessage);
            } else {
                PlayerCache.getToggled_2().remove(player.getUuid());
            }
        }
    }

    private void broadcastToStaff(PlayerRef sender, String message) {
        String staffChatPerm = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
        String prefix = HytaleMessages.PREFIX.get(String.class);
        String format = HytaleMessages.STAFFCHAT_FORMAT.get(String.class);

        String finalMessage = format
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{user}", sender.getUsername())
                .replace("{displayname}", sender.getUsername())
                .replace("{message}", message)
                .replace("{userprefix}", PermissionsUtil.getPrefix(sender.getUuid()))
                .replace("{usersuffix}", PermissionsUtil.getSuffix(sender.getUuid()))
                .replace("{server}", "");

        Message hytaleMsg = ChatColor.color((finalMessage));

        Universe.get().getPlayers().stream()
                .filter(p -> PermissionsUtil.hasPermission(p.getUuid(), staffChatPerm)
                        && !PlayerCache.getToggled().contains(p.getUuid()))
                .forEach(p -> p.sendMessage(hytaleMsg));
    }

    private void sendToDiscord(PlayerRef sender, String message) {
        if (!Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))
                || !Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class))
                || plugin.getJda() == null) {
            return;
        }

        TextChannel channel = plugin.getJda().getTextChannelById(HytaleDiscordConfig.STAFF_CHANNEL_ID.get(String.class));
        if (channel == null) return;

        String formatted = HytaleMessages.STAFFCHAT_FORMAT_DISCORD.get(String.class)
                .replace("{user}", sender.getUsername())
                .replace("{message}", message)
                .replace("{server}", "");

        if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(HytaleDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class));
            embed.setDescription(formatted);
            try {
                embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
            } catch (Exception ignored) {}
            embed.setFooter(HytaleDiscordConfig.EMBEDS_FOOTER.get(String.class));
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage(formatted).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannel().getId().equalsIgnoreCase(HytaleDiscordConfig.STAFF_CHANNEL_ID.get(String.class))) return;
        if (event.getAuthor().isBot() && !Boolean.TRUE.equals(HytaleDiscordConfig.FORWARD_BOT.get(Boolean.class))) return;

        if (event.getMessage().getContentDisplay().equalsIgnoreCase(HytaleMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class))) {
            event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        if (PlayerCache.getMuted().contains("true")) {
            event.getMessage().reply(HytaleMessages.STAFFCHAT_MUTED_ERROR_DISCORD.get(String.class)).queue();
            event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        String staffChatPerm = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
        String prefix = HytaleMessages.PREFIX.get(String.class);
        String discordFormat = HytaleMessages.DISCORD_STAFF_FORMAT.get(String.class);
        String nick = event.getMember() != null && event.getMember().getNickname() != null ? event.getMember().getNickname() : event.getAuthor().getName();

        String finalMessage = discordFormat
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{user}", event.getAuthor().getName())
                .replace("{username}", nick)
                .replace("{message}", event.getMessage().getContentDisplay());

        Message hytaleMsg = ChatColor.color((finalMessage));

        Universe.get().getPlayers().stream()
                .filter(p -> PermissionsUtil.hasPermission(p.getUuid(), staffChatPerm)
                        && !PlayerCache.getToggled().contains(p.getUuid()))
                .forEach(p -> p.sendMessage(hytaleMsg));
    }
}