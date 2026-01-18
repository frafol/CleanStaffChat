package it.frafol.cleanstaffchat.hytale.donorchat.listeners;

import com.hypixel.hytale.server.core.HytaleServer;
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
import it.frafol.cleanstaffchat.hytale.objects.LuckPermsUtil;
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

    public void onChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String message = event.getContent();
        String donorPrefix = HytaleMessages.DONORPREFIX.get(String.class);
        String usePerm = HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class);

        boolean isShortcut = Boolean.TRUE.equals(HytaleConfig.DONORCHAT_PREFIX_MODULE.get(Boolean.class))
                && message.startsWith(HytaleConfig.DONORCHAT_PREFIX.get(String.class));

        boolean isToggled = PlayerCache.getToggled_2_donor().contains(sender.getUuid());

        if (!isShortcut && !isToggled) {
            return;
        }

        if (!PermissionsModule.get().hasPermission(sender.getUuid(), usePerm)) {
            if (isToggled) PlayerCache.getToggled_2_donor().remove(sender.getUuid());
            return;
        }

        event.setCancelled(true);

        if (PlayerCache.getMuted().contains("true")) {
            String mutedMsg = HytaleMessages.DONORCHAT_MUTED_ERROR.get(String.class)
                    .replace("{prefix}", donorPrefix != null ? donorPrefix : "");
            sender.sendMessage(ChatColor.color((mutedMsg)));
            return;
        }

        if (PlayerCache.getCooldown().contains(sender.getUuid())) {
            String cooldownMsg = HytaleMessages.DONORCHAT_COOLDOWN_MESSAGE.get(String.class)
                    .replace("{prefix}", donorPrefix != null ? donorPrefix : "");
            sender.sendMessage(ChatColor.color((cooldownMsg)));
            return;
        }

        String prefixStr = HytaleConfig.DONORCHAT_PREFIX.get(String.class);
        String finalMessage = isShortcut ? message.substring(prefixStr.length()) : message;

        if (!PermissionsModule.get().hasPermission(sender.getUuid(), HytaleConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) {
            PlayerCache.getCooldown().add(sender.getUuid());
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() ->
                    PlayerCache.getCooldown().remove(sender.getUuid()), HytaleConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);
        }

        broadcastToDonors(sender, finalMessage);
        sendToDiscord(sender.getUsername(), finalMessage);
    }

    private void broadcastToDonors(PlayerRef sender, String message) {
        String donorPrefix = HytaleMessages.DONORPREFIX.get(String.class);
        String usePerm = HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class);
        String rawFormat = HytaleMessages.DONORCHAT_FORMAT.get(String.class);

        String finalMessage = rawFormat
                .replace("{prefix}", donorPrefix != null ? donorPrefix : "")
                .replace("{user}", sender.getUsername())
                .replace("{displayname}", sender.getUsername())
                .replace("{message}", message)
                .replace("{userprefix}", LuckPermsUtil.getPrefix(sender.getUuid()))
                .replace("{usersuffix}", LuckPermsUtil.getSuffix(sender.getUuid()))
                .replace("{server}", "");

        Message hytaleMsg = ChatColor.color((finalMessage));

        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                if (PermissionsModule.get().hasPermission(ref.getUuid(), usePerm)
                        && !PlayerCache.getToggled_donor().contains(ref.getUuid())) {
                    ref.sendMessage(hytaleMsg);
                }
            }
        });
    }

    private void sendToDiscord(String user, String message) {
        if (!Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))
                || !Boolean.TRUE.equals(HytaleConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class))) return;

        if (plugin.getJda() == null) return;
        TextChannel channel = plugin.getJda().getTextChannelById(HytaleDiscordConfig.DONOR_CHANNEL_ID.get(String.class));
        if (channel == null) return;

        String formatted = HytaleMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                .replace("{user}", user)
                .replace("{message}", message)
                .replace("{server}", "");

        if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(HytaleDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class));
            embed.setDescription(formatted);
            try {
                embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_DONORCHATCOLOR.get(String.class)));
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
        if (!event.getChannel().getId().equals(HytaleDiscordConfig.DONOR_CHANNEL_ID.get(String.class))) return;

        if (PlayerCache.getMuted_donor().contains("true")) return;

        if (PlayerCache.getCooldown_discord().contains(event.getAuthor().getId())
                && !Boolean.TRUE.equals(HytaleConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class))) {
            return;
        }

        String content = event.getMessage().getContentDisplay();
        String author = event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getName();
        String donorPrefix = HytaleMessages.DONORPREFIX.get(String.class);

        String rawFormat = HytaleMessages.DISCORD_DONOR_FORMAT.get(String.class);
        String formattedMessage = rawFormat
                .replace("{prefix}", donorPrefix != null ? donorPrefix : "")
                .replace("{user}", author)
                .replace("{username}", author)
                .replace("{message}", content);

        Message hytaleMsg = ChatColor.color((formattedMessage));

        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                if (PermissionsModule.get().hasPermission(ref.getUuid(), HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                        && !PlayerCache.getToggled_donor().contains(ref.getUuid())) {
                    ref.sendMessage(hytaleMsg);
                }
            }
        });

        if (!Boolean.TRUE.equals(HytaleConfig.COOLDOWN_BYPASS_DISCORD.get(Boolean.class))) {
            PlayerCache.getCooldown_discord().add(event.getAuthor().getId());
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() ->
                    PlayerCache.getCooldown_discord().remove(event.getAuthor().getId()), HytaleConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);
        }
    }
}