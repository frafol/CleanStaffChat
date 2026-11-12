package it.frafol.cleanstaffchat.bungee.objects;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeDiscordConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;

/** Sends plugin messages to Discord, as an embed or as plain text depending on the configuration. */
public final class DiscordBridge {

    private DiscordBridge() {}
    public static boolean isActive(BungeeConfig moduleToggle) {
        return BungeeDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && moduleToggle.get(Boolean.class);
    }

    public static void send(BungeeDiscordConfig channelId, BungeeDiscordConfig embedTitle, BungeeDiscordConfig embedColor, String text) {
        final JDA jda = CleanStaffChat.getInstance().getJda();
        if (jda == null) return;
        final TextChannel channel = jda.getTextChannelById(channelId.get(String.class));
        if (channel == null) return;
        if (BungeeDiscordConfig.USE_EMBED.get(Boolean.class)) {
            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(embedTitle.get(String.class), null);
            embed.setDescription(text);
            embed.setColor(parseColor(embedColor.get(String.class)));
            embed.setFooter(BungeeDiscordConfig.EMBEDS_FOOTER.get(String.class), null);
            channel.sendMessageEmbeds(embed.build()).queue();
            return;
        }
        channel.sendMessage(text).queue();
    }

    /** Parses values such as "0x00ff00" or "#00ff00"; returns null (default color) when invalid. */
    public static Color parseColor(String value) {
        if (value == null) return null;
        final String trimmed = value.trim();
        try {
            return Color.decode(trimmed.startsWith("#") ? "0x" + trimmed.substring(1) : trimmed);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
