package minej.minejango2.chatitemsprite.util;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerVersion {

    private static final Pattern VERSION_PREFIX = Pattern.compile("^(\\d+)(?:\\.(\\d+))?");

    // Beds became actual block from block entity via 26.2 (Chaos Cubed) Update
    private static final boolean BEDS_ARE_REGULAR_BLOCKS = computeAtLeast(26, 2);

    private ServerVersion() {}

    public static boolean bedsAreRegularBlocks() {
        return BEDS_ARE_REGULAR_BLOCKS;
    }

    /**
     * Check the server version is higher than the value
     * If it failed to parse (like JE's version format changed), code consider as recent versions
     */
    private static boolean computeAtLeast(int major, int minor) {
        String raw = Bukkit.getMinecraftVersion(); // 예: "26.1", "26.3"
        Matcher matcher = VERSION_PREFIX.matcher(raw);

        if (!matcher.find()) {
            return true;
        }

        int actualMajor = Integer.parseInt(matcher.group(1));
        int actualMinor = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;

        if (actualMajor != major) {
            return actualMajor > major;
        }
        return actualMinor >= minor;
    }
}
