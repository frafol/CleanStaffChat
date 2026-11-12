package it.frafol.cleanstaffchat.bungee.objects;

/** Prefix and suffix of a player as provided by the permission plugin. */
public final class Affixes {

    public static final Affixes NONE = new Affixes("", "");

    private final String prefix;
    private final String suffix;

    public Affixes(String prefix, String suffix) {
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
    }

    public String prefix() {
        return prefix;
    }

    public String suffix() {
        return suffix;
    }

    public String coloredPrefix() {
        return PlayerCache.translateHex(prefix);
    }

    public String coloredSuffix() {
        return PlayerCache.translateHex(suffix);
    }

    public String displayName(String name) {
        return coloredPrefix() + name + coloredSuffix();
    }
}
