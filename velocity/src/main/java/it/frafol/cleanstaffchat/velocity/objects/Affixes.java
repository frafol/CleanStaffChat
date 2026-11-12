package it.frafol.cleanstaffchat.velocity.objects;

import it.frafol.cleanstaffchat.velocity.utils.ChatUtil;

/** Prefix and suffix of a player as provided by LuckPerms. */
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
        return ChatUtil.translateHex(prefix);
    }

    public String coloredSuffix() {
        return ChatUtil.translateHex(suffix);
    }

    public String displayName(String name) {
        return coloredPrefix() + name + coloredSuffix();
    }
}
