package it.frafol.cleanstaffchat.hytale.donorchat.commands;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
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

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DonorChatCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public DonorChatCommand(CleanStaffChat plugin, String name, String description, java.util.List<String> aliases) {
        super(name, description);
        this.plugin = plugin;
        this.setAllowsExtraArguments(true);
        this.requirePermission(Objects.requireNonNull(HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class)));
        if (aliases != null) {
            this.addAliases(aliases.toArray(new String[0]));
        }
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        String permission = HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class);
        boolean senderHasPerm = PermissionsUtil.hasPermission(sender.getUuid(), permission);

        String input = context.getInputString().trim();
        String[] split = input.split("\\s+", 2);
        String messageArg = split.length > 1 ? split[1] : "";

        if (messageArg.isEmpty()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(HytaleMessages.PLAYER_ONLY.color());
                return CompletableFuture.completedFuture(null);
            }

            if (senderHasPerm) {
                if (!PlayerCache.getToggled_2_donor().contains(sender.getUuid())) {
                    if (!Boolean.TRUE.equals(HytaleConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {
                        sender.sendMessage(HytaleMessages.DONORARGUMENTS.color());
                        return CompletableFuture.completedFuture(null);
                    }

                    if (!PlayerCache.getMuted().contains("true")) {
                        PlayerCache.getToggled_2_donor().add(sender.getUuid());
                        PlayerCache.getToggled_2().remove(sender.getUuid());
                        PlayerCache.getToggled_2_admin().remove(sender.getUuid());
                        sender.sendMessage(HytaleMessages.DONORCHAT_TALK_ENABLED.color());
                    }
                } else {
                    PlayerCache.getToggled_2_donor().remove(sender.getUuid());
                    sender.sendMessage(HytaleMessages.DONORCHAT_TALK_DISABLED.color());
                }
            } else {
                if (Boolean.FALSE.equals(HytaleConfig.HIDE_ADVERTS.get(Boolean.class))) {
                    sender.sendMessage(ChatColor.color(("This server is using CleanStaffChat by frafol")));
                }
            }
            return CompletableFuture.completedFuture(null);
        }

        if (!senderHasPerm) {
            sender.sendMessage(HytaleMessages.NO_PERMISSION.color());
            return CompletableFuture.completedFuture(null);
        }

        if (PlayerCache.getMuted().contains("true")) {
            sender.sendMessage(HytaleMessages.DONORCHAT_MUTED_ERROR.color());
            return CompletableFuture.completedFuture(null);
        }

        if (sender instanceof Player && PlayerCache.getCooldown().contains(sender.getUuid())) {
            sender.sendMessage(HytaleMessages.DONORCHAT_COOLDOWN_MESSAGE.color());
            return CompletableFuture.completedFuture(null);
        }

        String senderName = (sender instanceof Player) ? sender.getDisplayName() : HytaleConfig.CONSOLE_PREFIX.get(String.class);
        String rawFormat = HytaleMessages.DONORCHAT_FORMAT.get(String.class);
        String prefix = HytaleMessages.DONORPREFIX.get(String.class);

        String formatted = rawFormat
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{user}", senderName)
                .replace("{displayname}", senderName)
                .replace("{message}", messageArg)
                .replace("{userprefix}", PermissionsUtil.getPrefix(sender.getUuid()))
                .replace("{usersuffix}", PermissionsUtil.getSuffix(sender.getUuid()))
                .replace("{server}", "");

        Message hytaleMsg = ChatColor.color((formatted));
        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                if (PermissionsUtil.hasPermission(ref.getUuid(), permission)
                        && !PlayerCache.getToggled_donor().contains(ref.getUuid())) {
                    ref.sendMessage(hytaleMsg);
                }
            }
        });

        if (!(sender instanceof Player)) sender.sendMessage(hytaleMsg);
        sendToDiscord(senderName, messageArg);

        if (sender instanceof Player && !PermissionsUtil.hasPermission(sender.getUuid(), HytaleConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) {
            PlayerCache.getCooldown().add(sender.getUuid());
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() ->
                            PlayerCache.getCooldown().remove(sender.getUuid()),
                    HytaleConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);
        }

        return CompletableFuture.completedFuture(null);
    }

    private void sendToDiscord(String user, String message) {
        if (Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class))
                && Boolean.TRUE.equals(HytaleConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class))) {

            if (plugin.getJda() == null) return;
            TextChannel channel = plugin.getJda().getTextChannelById(HytaleDiscordConfig.DONOR_CHANNEL_ID.get(String.class));
            if (channel == null) return;

            String discordFormat = HytaleMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                    .replace("{user}", user)
                    .replace("{message}", message);

            if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(HytaleDiscordConfig.DONORCHAT_EMBED_TITLE.get(String.class));
                embed.setDescription(discordFormat);
                try {
                    embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_DONORCHATCOLOR.get(String.class)));
                } catch (Exception ignored) {}
                channel.sendMessageEmbeds(embed.build()).queue();
            } else {
                channel.sendMessage(discordFormat).queue();
            }
        }
    }
}