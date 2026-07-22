package minej.minejango2.chatitemsprite.updater;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionComparator {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-beta-(\\d+))?$");

    private VersionComparator() {}

    public static boolean isNewer(String current, String latest) {
        return compare(current, latest) < 0;
    }

    public static Optional<Boolean> tryIsNewer(String current, String latest) {
        try {
            return Optional.of(isNewer(current, latest));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static int compare(String current, String latest) {
        Version a = parse(current);
        Version b = parse(latest);

        int result;

        result = Integer.compare(a.major, b.major);
        if (result != 0) return result;

        result = Integer.compare(a.minor, b.minor);
        if (result != 0) return result;

        result = Integer.compare(a.patch, b.patch);
        if (result != 0) return result;

        // Release > Beta
        if (a.beta == null && b.beta != null) {
            return 1;
        }

        if (a.beta != null && b.beta == null) {
            return -1;
        }

        if (a.beta == null) {
            return 0;
        }

        return Integer.compare(a.beta, b.beta);
    }

    private static Version parse(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }

        return new Version(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                matcher.group(4) == null ? null : Integer.parseInt(matcher.group(4))
        );
    }

    private record Version(int major, int minor, int patch, Integer beta) {
    }
}
