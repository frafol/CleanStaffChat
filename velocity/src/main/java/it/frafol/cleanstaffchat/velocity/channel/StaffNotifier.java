package it.frafol.cleanstaffchat.velocity.channel;

import com.velocitypowered.api.proxy.Player;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.hooks.RedisListener;
import it.frafol.cleanstaffchat.velocity.objects.AffixResolver;
import it.frafol.cleanstaffchat.velocity.objects.Affixes;
import it.frafol.cleanstaffchat.velocity.objects.DiscordBridge;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;

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
     * @param server        current server name ("" when unknown)
     */
    public static void announce(Player subject, VelocityMessages gameFormat, VelocityMessages discordFormat,
                                VelocityConfig discordToggle, boolean afk, String serverBefore, String server) {

        final Affixes affixes = AffixResolver.resolve(subject);
        if (affixes == null) return;

        final ChatChannel staff = ChatChannel.STAFF;
        final String name = subject.getUsername();
        final String before = serverBefore == null ? "" : serverBefore;

        if (ChannelMessenger.isRedisEnabled()) {
            final String line = gameFormat.get(String.class)
                    .replace("%prefix%", staff.prefix())
                    .replace("%user%", name)
                    .replace("%displayname%", affixes.displayName(name))
                    .replace("%userprefix%", affixes.coloredPrefix())
                    .replace("%usersuffix%", affixes.coloredSuffix())
                    .replace("%serverbefore%", before)
                    .replace("%server%", server)
                    .replace("&", "§");
            ChannelMessenger.publish(afk ? RedisListener.STAFF_AFK_CHANNEL : RedisListener.STAFF_OTHER_CHANNEL, line);
        } else {
            ChannelMessenger.broadcastLocal(staff, gameFormat,
                    new Placeholder("user", name),
                    new Placeholder("displayname", affixes.displayName(name)),
                    new Placeholder("userprefix", affixes.coloredPrefix()),
                    new Placeholder("usersuffix", affixes.coloredSuffix()),
                    new Placeholder("serverbefore", before),
                    new Placeholder("server", server),
                    new Placeholder("prefix", staff.prefix()));
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
