package it.frafol.cleanstaffchat.bungee.channel;

import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.hooks.RedisListener;
import it.frafol.cleanstaffchat.bungee.objects.AffixResolver;
import it.frafol.cleanstaffchat.bungee.objects.Affixes;
import it.frafol.cleanstaffchat.bungee.objects.DiscordBridge;
import it.frafol.cleanstaffchat.bungee.objects.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Staff notifications about a player (join, quit, switch, AFK on/off): staff chat recipients
 * (locally or through RedisBungee) plus Discord.
 */
public final class StaffNotifier {

    private StaffNotifier() {
    }

    /**
     * @param gameFormat    in-game message
     * @param discordFormat Discord message
     * @param discordToggle extra module toggle that must be on to forward to Discord
     * @param afk           true for AFK notices, which travel on their own Redis channel
     * @param serverBefore  previous server for switch notices, otherwise null
     */
    public static void announce(ProxiedPlayer subject, BungeeMessages gameFormat, BungeeMessages discordFormat,
                                BungeeConfig discordToggle, boolean afk, String serverBefore) {

        final Affixes affixes = AffixResolver.resolve(subject);
        if (affixes == null) return;

        final ChatChannel staff = ChatChannel.STAFF;
        final String name = subject.getName();
        final String server = subject.getServer() == null ? "" : subject.getServer().getInfo().getName();
        final String before = serverBefore == null ? "" : serverBefore;

        final String line = gameFormat.color()
                .replace("%prefix%", staff.prefix())
                .replace("%user%", name)
                .replace("%displayname%", affixes.displayName(name))
                .replace("%userprefix%", affixes.coloredPrefix())
                .replace("%usersuffix%", affixes.coloredSuffix())
                .replace("%serverbefore%", before)
                .replace("%server%", server)
                .replace("&", "§");

        if (ChannelMessenger.isRedisEnabled()) {
            ChannelMessenger.publish(afk ? RedisListener.STAFF_AFK_CHANNEL : RedisListener.STAFF_OTHER_CHANNEL, line);
        } else {
            ChannelMessenger.broadcastLocal(staff, line);
        }

        if (DiscordBridge.isActive(staff.settings.discordModule) && discordToggle.get(Boolean.class)) {
            final String text = discordFormat.get(String.class)
                    .replace("%user%", name)
                    .replace("%userprefix%", Placeholder.stripToDiscord(affixes.prefix()))
                    .replace("%usersuffix%", Placeholder.stripToDiscord(affixes.suffix()))
                    .replace("%serverbefore%", before)
                    .replace("%server%", server);
            DiscordBridge.send(staff.discord.channelId, staff.discord.embedTitle, staff.discord.embedColor, text);
        }
    }
}
