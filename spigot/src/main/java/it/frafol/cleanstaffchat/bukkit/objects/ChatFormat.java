package it.frafol.cleanstaffchat.bukkit.objects;

/**
 * Shared placeholder replacement for every in-game message the plugin builds.
 */
public final class ChatFormat {

    private ChatFormat() {
    }

    /**
     * Replaces every placeholder except %message%, which callers append last so that
     * user-supplied text is never scanned for placeholders.
     */
    public static String apply(String template, String channelPrefix, String name, Affixes affixes) {
        return template
                .replace("%prefix%", channelPrefix)
                .replace("%user%", name)
                .replace("%displayname%", affixes.displayName(name))
                .replace("%userprefix%", affixes.coloredPrefix())
                .replace("%usersuffix%", affixes.coloredSuffix())
                .replace("%server%", "");
    }

    /** Same replacements for the Discord side (colors stripped). */
    public static String applyDiscord(String template, String name, Affixes affixes) {
        return template
                .replace("%user%", name)
                .replace("%userprefix%", Placeholder.stripToDiscord(affixes.prefix()))
                .replace("%usersuffix%", Placeholder.stripToDiscord(affixes.suffix()))
                .replace("%server%", "");
    }
}
