package it.frafol.cleanstaffchat.bukkit.channel;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotMessages;
import it.frafol.cleanstaffchat.bukkit.objects.AffixResolver;
import it.frafol.cleanstaffchat.bukkit.objects.Affixes;
import it.frafol.cleanstaffchat.bukkit.objects.ChatFormat;
import it.frafol.cleanstaffchat.bukkit.objects.DiscordBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Staff notifications about a player (join, quit, AFK on/off): staff chat recipients plus Discord.
 */
public final class StaffNotifier {

    private StaffNotifier() {
    }

    /**
     * @param gameFormat    in-game message
     * @param discordFormat Discord message (supports %user%, %userprefix%, %usersuffix%)
     * @param discordToggle extra module toggle that must be on to forward to Discord
     * @param skipHidden    true to skip staff who switched the staff chat off
     */
    public static void announce(CleanStaffChat plugin, Player subject, SpigotMessages gameFormat,
                                SpigotMessages discordFormat, SpigotConfig discordToggle, boolean skipHidden) {

        final Affixes affixes = AffixResolver.resolve(subject);
        if (affixes == null) return;

        final ChatChannel staff = ChatChannel.STAFF;
        final String line = ChatFormat.apply(gameFormat.color(subject), staff.prefix(), subject.getName(), affixes);
        final String permission = staff.settings.usePermission.get(String.class);

        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (!recipient.hasPermission(permission)) continue;
            if (skipHidden && staff.isHidden(recipient.getUniqueId())) continue;
            recipient.sendMessage(line);
        }

        if (DiscordBridge.isActive(staff.settings.discordModule) && discordToggle.get(Boolean.class)) {
            DiscordBridge.send(plugin, staff.discord.channelId, staff.discord.embedTitle, staff.discord.embedColor,
                    ChatFormat.applyDiscord(discordFormat.get(String.class), subject.getName(), affixes));
        }
    }
}
