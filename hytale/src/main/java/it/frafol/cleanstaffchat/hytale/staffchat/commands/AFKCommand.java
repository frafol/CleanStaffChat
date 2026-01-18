package it.frafol.cleanstaffchat.hytale.staffchat.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
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

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AFKCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public AFKCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
        super(name, description);
        this.plugin = plugin;
        this.setAllowsExtraArguments(true);
        if (aliases != null) {
            this.addAliases(aliases.toArray(String[]::new));
        }
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (!Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_AFK_MODULE.get(Boolean.class))) {
            String disabledMsg = HytaleMessages.MODULE_DISABLED.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((disabledMsg)));
            return CompletableFuture.completedFuture(null);
        }

        if (sender.getUuid() == null) {
            String playerOnly = HytaleMessages.PLAYER_ONLY.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((playerOnly)));
            return CompletableFuture.completedFuture(null);
        }

        if (!PermissionsModule.get().hasPermission(sender.getUuid(), Objects.requireNonNull(HytaleConfig.STAFFCHAT_AFK_PERMISSION.get(String.class)))) {
            String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                    .replace("{prefix}", HytaleMessages.PREFIX.get(String.class));
            sender.sendMessage(ChatColor.color((noPerm)));
            return CompletableFuture.completedFuture(null);
        }

        boolean isNowAfk = !PlayerCache.getAfk().contains(sender.getUuid());

        if (isNowAfk) {
            PlayerCache.getAfk().add(sender.getUuid());
            broadcastAfkMessage(sender, HytaleMessages.STAFFCHAT_AFK_ON);
            sendDiscordAfkMessage(sender, true);
        } else {
            PlayerCache.getAfk().remove(sender.getUuid());
            broadcastAfkMessage(sender, HytaleMessages.STAFFCHAT_AFK_OFF);
            sendDiscordAfkMessage(sender, false);
        }

        return CompletableFuture.completedFuture(null);
    }

    private void broadcastAfkMessage(CommandSender sender, HytaleMessages messageEnum) {
        String usePermission = HytaleConfig.STAFFCHAT_USE_PERMISSION.get(String.class);
        String prefix = HytaleMessages.PREFIX.get(String.class);
        String rawFormat = messageEnum.get(String.class);

        String finalMessage = rawFormat
                .replace("{prefix}", prefix != null ? prefix : "")
                .replace("{user}", sender.getDisplayName())
                .replace("{displayname}", sender.getDisplayName())
                .replace("{userprefix}", PermissionsUtil.getPrefix(sender.getUuid()))
                .replace("{usersuffix}", PermissionsUtil.getSuffix(sender.getUuid()))
                .replace("{server}", "");

        Message hytaleMsg = ChatColor.color((finalMessage));

        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                assert usePermission != null;
                if (PermissionsModule.get().hasPermission(ref.getUuid(), usePermission)
                        && !PlayerCache.getToggled().contains(ref.getUuid())) {
                    ref.sendMessage(hytaleMsg);
                }
            }
        });
    }

    private void sendDiscordAfkMessage(CommandSender sender, boolean goingAfk) {
        if (!Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) ||
                !Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_MODULE.get(Boolean.class)) ||
                !Boolean.TRUE.equals(HytaleConfig.STAFFCHAT_DISCORD_AFK_MODULE.get(Boolean.class))) {
            return;
        }

        if (plugin.getJda() == null) return;

        TextChannel channel = plugin.getJda().getTextChannelById(HytaleDiscordConfig.STAFF_CHANNEL_ID.get(String.class));
        if (channel == null) return;

        String messageFormat = goingAfk
                ? HytaleMessages.STAFF_DISCORD_AFK_ON_MESSAGE_FORMAT.get(String.class)
                : HytaleMessages.STAFF_DISCORD_AFK_OFF_MESSAGE_FORMAT.get(String.class);

        String finalMessage = messageFormat.replace("{user}", sender.getDisplayName());

        if (Boolean.TRUE.equals(HytaleDiscordConfig.USE_EMBED.get(Boolean.class))) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(HytaleDiscordConfig.STAFFCHAT_EMBED_TITLE.get(String.class));
            embed.setDescription(finalMessage);
            try {
                embed.setColor(Color.decode(HytaleDiscordConfig.EMBEDS_STAFFCHATCOLOR.get(String.class)));
            } catch (Exception ignored) {}
            embed.setFooter(HytaleDiscordConfig.EMBEDS_FOOTER.get(String.class));
            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.sendMessage(finalMessage).queue();
        }
    }
}