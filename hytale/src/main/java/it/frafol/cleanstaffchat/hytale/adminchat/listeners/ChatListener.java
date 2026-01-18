package it.frafol.cleanstaffchat.hytale.adminchat.listeners;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ChatListener extends ListenerAdapter {

    private final CleanStaffChat plugin;

    public ChatListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    public void onChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String message = event.getContent();
        String adminPrefix = HytaleMessages.ADMINPREFIX.get(String.class);
        String usePerm = HytaleConfig.ADMINCHAT_USE_PERMISSION.get(String.class);

        boolean isShortcut = Boolean.TRUE.equals(HytaleConfig.ADMINCHAT_PREFIX_MODULE.get(Boolean.class))
                && message.startsWith(HytaleConfig.ADMINCHAT_PREFIX.get(String.class));

        boolean isToggled = PlayerCache.getToggled_2_admin().contains(sender.getUuid());

        if (!isShortcut && !isToggled) {
            return;
        }

        if (!PermissionsUtil.hasPermission(sender.getUuid(), usePerm)) {
            if (isToggled) PlayerCache.getToggled_2_admin().remove(sender.getUuid());
            return;
        }

        event.setCancelled(true);

        if (PlayerCache.getMuted().contains("true")) {
            String mutedMsg = HytaleMessages.ADMINCHAT_MUTED_ERROR.get(String.class)
                    .replace("{prefix}", adminPrefix != null ? adminPrefix : "");
            sender.sendMessage(ChatColor.color((mutedMsg)));
            return;
        }

        String prefixStr = HytaleConfig.ADMINCHAT_PREFIX.get(String.class);
        String finalMessage = isShortcut ? message.substring(prefixStr.length()) : message;

        String rawFormat = HytaleMessages.ADMINCHAT_FORMAT.get(String.class);
        String formattedMessage = rawFormat
                .replace("{prefix}", adminPrefix != null ? adminPrefix : "")
                .replace("{user}", sender.getUsername())
                .replace("{displayname}", sender.getUsername())
                .replace("{message}", finalMessage)
                .replace("{userprefix}", PermissionsUtil.getPrefix(sender.getUuid()))
                .replace("{usersuffix}", PermissionsUtil.getSuffix(sender.getUuid()))
                .replace("{server}", "");

        Message hytaleMsg = ChatColor.color((formattedMessage));

        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                if (PermissionsUtil.hasPermission(ref.getUuid(), usePerm)
                        && !PlayerCache.getToggled_admin().contains(ref.getUuid())) {
                    ref.sendMessage(hytaleMsg);
                }
            }
        });

        sendToDiscord(sender.getUsername(), finalMessage);
    }

    private void sendToDiscord(String user, String message) {
        if (!Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))
                || !Boolean.TRUE.equals(HytaleConfig.ADMINCHAT_DISCORD_MODULE.get(Boolean.class))) return;

        if (plugin.getJda() == null) return;
        TextChannel channel = plugin.getJda().getTextChannelById(HytaleDiscordConfig.ADMIN_CHANNEL_ID.get(String.class));
        if (channel == null) return;

        String formatted = HytaleMessages.ADMINCHAT_FORMAT_DISCORD.get(String.class)
                .replace("{user}", user)
                .replace("{message}", message)
                .replace("{server}", "");

        if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(HytaleDiscordConfig.ADMINCHAT_EMBED_TITLE.get(String.class));
            embed.setDescription(formatted);
            try {
                embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_ADMINCHATCOLOR.get(String.class)));
            } catch (Exception ignored) {}
            embed.setFooter(HytaleDiscordConfig.EMBEDS_FOOTER.get(String.class));
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage(formatted).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && !Boolean.TRUE.equals(HytaleDiscordConfig.FORWARD_BOT.get(Boolean.class))) return;
        if (!event.getChannel().getId().equals(HytaleDiscordConfig.ADMIN_CHANNEL_ID.get(String.class))) return;

        String content = event.getMessage().getContentDisplay();
        String author = event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getName();
        String adminPrefix = HytaleMessages.ADMINPREFIX.get(String.class);

        String rawFormat = HytaleMessages.DISCORD_ADMIN_FORMAT.get(String.class);
        String formattedMessage = rawFormat
                .replace("{prefix}", adminPrefix != null ? adminPrefix : "")
                .replace("{user}", author)
                .replace("{username}", author)
                .replace("{message}", content);

        Message hytaleMsg = ChatColor.color((formattedMessage));

        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                if (PermissionsUtil.hasPermission(ref.getUuid(), HytaleConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                        && !PlayerCache.getToggled_admin().contains(ref.getUuid())) {
                    ref.sendMessage(hytaleMsg);
                }
            }
        });
    }
}