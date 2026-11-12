package it.frafol.cleanstaffchat.bukkit.objects;

import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotConfig;
import it.frafol.cleanstaffchat.bukkit.enums.SpigotDiscordConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;

/**
 * Sends plugin messages to Discord, as an embed or as plain text depending on the configuration.
 */
public final class DiscordBridge {

    private DiscordBridge() {}

    /** True when Discord is enabled globally and the given module toggle is on. */
    public static boolean isActive(SpigotConfig moduleToggle) {
        return SpigotDiscordConfig.DISCORD_ENABLED.get(Boolean.class) && moduleToggle.get(Boolean.class);
    }

    public static void send(CleanStaffChat plugin, SpigotDiscordConfig channelId, SpigotDiscordConfig embedTitle, SpigotDiscordConfig embedColor, String text) {
        final JDA jda = plugin.getJda();
        if (jda == null) return;
        final TextChannel channel = jda.getTextChannelById(channelId.get(String.class));
        if (channel == null) return;
        if (SpigotDiscordConfig.USE_EMBED.get(Boolean.class)) {
            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(embedTitle.get(String.class), null);
            embed.setDescription(text);
            embed.setColor(parseColor(embedColor.get(String.class)));
            embed.setFooter(SpigotDiscordConfig.EMBEDS_FOOTER.get(String.class), null);
            channel.sendMessageEmbeds(embed.build()).queue();
            return;
        }
        channel.sendMessage(text).queue();
    }

    /** Parses values such as "0x00ff00" or "#00ff00"; returns null (default color) when invalid. */
    public static Color parseColor(String value) {
        if (value == null) return null;
        try {
            return Color.decode(value.trim().startsWith("#") ? "0x" + value.trim().substring(1) : value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
