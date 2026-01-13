package it.frafol.cleanstaffchat.hytale.donorchat.commands;

import com.hypixel.hytale.server.core.HytaleServer;
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
import it.frafol.cleanstaffchat.hytale.objects.PlayerCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DonorChatCommand extends AbstractCommand {

    private final CleanStaffChat plugin;

    public DonorChatCommand(CleanStaffChat plugin, String name, String description, List<String> aliases) {
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
        String input = context.getInputString();
        String[] allArgs = input.trim().isEmpty() ? new String[0] : input.split("\\s+");
        String[] args = allArgs.length > 0 && allArgs[0].equalsIgnoreCase(getName()) ?
                java.util.Arrays.copyOfRange(allArgs, 1, allArgs.length) : allArgs;

        String prefix = HytaleMessages.DONORPREFIX.get(String.class);

        if (!PermissionsModule.get().hasPermission(sender.getUuid(), HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {
            if (!Boolean.TRUE.equals(HytaleConfig.HIDE_ADVERTS.get(Boolean.class))) {
                sender.sendMessage(ChatColor.color(("This server is using CleanStaffChat by frafol.")));
            } else {
                String noPerm = HytaleMessages.NO_PERMISSION.get(String.class)
                        .replace("{prefix}", prefix != null ? prefix : "");
                sender.sendMessage(ChatColor.color((noPerm)));
            }
            return CompletableFuture.completedFuture(null);
        }

        if (args.length == 0) {
            if (sender.getUuid() == null) {
                String playerOnly = HytaleMessages.PLAYER_ONLY.get(String.class)
                        .replace("{prefix}", prefix != null ? prefix : "");
                sender.sendMessage(ChatColor.color((playerOnly)));
                return CompletableFuture.completedFuture(null);
            }

            if (!Boolean.TRUE.equals(HytaleConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {
                String donorArgs = HytaleMessages.DONORARGUMENTS.get(String.class)
                        .replace("{prefix}", prefix != null ? prefix : "");
                sender.sendMessage(ChatColor.color((donorArgs)));
                return CompletableFuture.completedFuture(null);
            }

            if (PlayerCache.getToggled_2_donor().contains(sender.getUuid())) {
                PlayerCache.getToggled_2_donor().remove(sender.getUuid());
                String disabled = HytaleMessages.DONORCHAT_TALK_DISABLED.get(String.class)
                        .replace("{prefix}", prefix != null ? prefix : "");
                sender.sendMessage(ChatColor.color((disabled)));
            } else {
                if (!PlayerCache.getMuted().contains("true")) {
                    PlayerCache.getToggled_2_donor().add(sender.getUuid());
                    PlayerCache.getToggled_2_admin().remove(sender.getUuid());
                    PlayerCache.getToggled_2().remove(sender.getUuid());
                    String enabled = HytaleMessages.DONORCHAT_TALK_ENABLED.get(String.class)
                            .replace("{prefix}", prefix != null ? prefix : "");
                    sender.sendMessage(ChatColor.color((enabled)));
                } else {
                    String muted = HytaleMessages.DONORCHAT_MUTED_ERROR.get(String.class)
                            .replace("{prefix}", prefix != null ? prefix : "");
                    sender.sendMessage(ChatColor.color((muted)));
                }
            }
            return CompletableFuture.completedFuture(null);
        }

        if (PlayerCache.getMuted().contains("true")) {
            String muted = HytaleMessages.DONORCHAT_MUTED_ERROR.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((muted)));
            return CompletableFuture.completedFuture(null);
        }

        String message = String.join(" ", args);

        if (sender.getUuid() != null && PlayerCache.getCooldown().contains(sender.getUuid())) {
            String cooldown = HytaleMessages.DONORCHAT_COOLDOWN_MESSAGE.get(String.class)
                    .replace("{prefix}", prefix != null ? prefix : "");
            sender.sendMessage(ChatColor.color((cooldown)));
            return CompletableFuture.completedFuture(null);
        }

        broadcastDonorMessage(sender, message);
        sendDiscordMessage(sender, message);

        if (sender.getUuid() != null && !PermissionsModule.get().hasPermission(sender.getUuid(), HytaleConfig.COOLDOWN_BYPASS_PERMISSION.get(String.class))) {
            PlayerCache.getCooldown().add(sender.getUuid());
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() ->
                            PlayerCache.getCooldown().remove(sender.getUuid()),
                    HytaleConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);
        }

        return CompletableFuture.completedFuture(null);
    }

    private void broadcastDonorMessage(CommandSender sender, String message) {
        String donorPrefix = HytaleMessages.DONORPREFIX.get(String.class);
        String usePerm = HytaleConfig.DONORCHAT_USE_PERMISSION.get(String.class);
        String rawFormat = HytaleMessages.DONORCHAT_FORMAT.get(String.class);

        String finalMessage = rawFormat
                .replace("{prefix}", donorPrefix != null ? donorPrefix : "")
                .replace("{user}", sender.getDisplayName())
                .replace("{displayname}", sender.getDisplayName())
                .replace("{message}", message)
                .replace("{userprefix}", "")
                .replace("{usersuffix}", "")
                .replace("{server}", "");

        Message hytaleMsg = ChatColor.color((finalMessage));

        Universe.get().getWorlds().values().forEach(world -> {
            for (PlayerRef ref : world.getPlayerRefs()) {
                if (usePerm != null && PermissionsModule.get().hasPermission(ref.getUuid(), usePerm)
                        && !PlayerCache.getToggled_donor().contains(ref.getUuid())) {
                    ref.sendMessage(hytaleMsg);
                }
            }
        });
    }

    private void sendDiscordMessage(CommandSender sender, String message) {
        if (!Boolean.TRUE.equals(HytaleDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) ||
                !Boolean.TRUE.equals(HytaleConfig.DONORCHAT_DISCORD_MODULE.get(Boolean.class))) return;

        if (plugin.getJda() == null) return;
        TextChannel channel = plugin.getJda().getTextChannelById(HytaleDiscordConfig.DONOR_CHANNEL_ID.get(String.class));
        if (channel == null) return;

        String formatted = HytaleMessages.DONORCHAT_FORMAT_DISCORD.get(String.class)
                .replace("{user}", sender.getDisplayName())
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
}