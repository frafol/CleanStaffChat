package it.frafol.cleanstaffchat.bukkit.objects;

/**
 * Prefix and suffix of a player as provided by the permission plugin (LuckPerms / UltraPermissions).
 */
public final class Affixes {

    public static final Affixes NONE = new Affixes("", "");

    private final String prefix;
    private final String suffix;

    public Affixes(String prefix, String suffix) {
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
    }

    /** Raw prefix, as stored by the permission plugin. */
    public String prefix() {
        return prefix;
    }

    /** Raw suffix, as stored by the permission plugin. */
    public String suffix() {
        return suffix;
    }

    public String coloredPrefix() {
        return PlayerCache.color(prefix);
    }

    public String coloredSuffix() {
        return PlayerCache.color(suffix);
    }

    public String displayName(String name) {
        return coloredPrefix() + name + coloredSuffix();
    }
}
