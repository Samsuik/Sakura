package me.samsuik.sakura.mechanics;

/**
 * Encode Minecraft versions into a short.
 * <p>
 * <p> Format: [significant][major][snapshot][minor]
 * <p> - 4 significant bits (1, 26 -> 40)
 * <p> - 5 major bits       (0 -> 31)
 * <p> - 1 snapshot bit
 * <p> - 6 minor bits       (0 -> 63)
 * <b>
 * <p>The legacy snapshot format [year]w[week][numeral] is unsupported. This would require mappings to
 * map legacy snapshots to a target release to be used with the modern snapshot format.
 *
 */
public final class MinecraftVersionEncoding {
    private static final int SIGNIFICANT_BITS = 4;
    private static final int MAJOR_BITS = 5;
    private static final int MINOR_BITS = 6;

    private static final int SIGNIFICANT_SHIFT = Short.SIZE - SIGNIFICANT_BITS;
    private static final int MAJOR_SHIFT       = SIGNIFICANT_SHIFT - MAJOR_BITS;
    private static final int MINOR_SHIFT       = MAJOR_SHIFT - 1 - MINOR_BITS;

    private static final int MAJOR_MASK = (1 << MAJOR_BITS) - 1;
    private static final int MINOR_MASK = (1 << MINOR_BITS) - 1;

    private static final int LEGACY_VERSION = 1;
    private static final int MODERN_VERSION_START = 26;
    private static final int SNAPSHOT_BIT = 1 << (MAJOR_SHIFT - 1);

    private static final String SNAPSHOT_TELLTALE = "-snapshot-";
    private static final String SNAPSHOT_FORMAT = "%s.%s-snapshot-%s";
    private static final String TRADITIONAL_FORMAT = "%s.%s.%s";

    private static short packedSignificantBits(final int version) {
        if (version == LEGACY_VERSION) {
            return 0;
        }
        return (short) (version - MODERN_VERSION_START + 1);
    }

    private static short unpackedSignificantBits(final int version) {
        if (version == 0) {
            return LEGACY_VERSION;
        }
        return (short) (version + MODERN_VERSION_START - 1);
    }

    /**
     * Encodes a Minecraft snapshot version into a short.
     *
     * @param year  the year
     * @param drop  the game drop
     * @param patch the patch
     * @return the encoded snapshot version as a short
     */
    public static short snapshot(final int year, final int drop, final int patch) {
        short encoded = encode(year, drop, patch);
        encoded |= SNAPSHOT_BIT;
        return encoded;
    }

    /**
     * Encodes a legacy 1.x.y Minecraft version into a short.
     *
     * @param major the major version (x)
     * @param minor the minor version (y)
     * @return the encoded version as a short
     */
    public static short v1xy(final int major, final int minor) {
        return encode(1, major, minor);
    }

    /**
     * Encodes a Minecraft version into a short.
     *
     * @param significant the significant version
     * @param major       the major version
     * @param minor       the minor version
     * @return the encoded version as a short
     */
    public static short encode(final int significant, final int major, final int minor) {
        short encoded = 0;
        encoded |= (short) (packedSignificantBits(significant) << SIGNIFICANT_SHIFT);
        encoded |= (short) ((major & MAJOR_MASK) << MAJOR_SHIFT);
        encoded |= (short) ((minor & MINOR_MASK) << MINOR_SHIFT);
        return encoded;
    }

    /**
     * Whether an encoded version is a Minecraft snapshot.
     *
     * @param version the encoded version
     * @return if the version is a snapshot
     */
    public static boolean isSnapshot(final short version) {
        return (version & SNAPSHOT_BIT) != 0;
    }

    /**
     * Decodes the significant version from an encoded version.
     *
     * @param version the encoded version
     * @return the significant version
     */
    public static int significant(final short version) {
        return unpackedSignificantBits(Short.toUnsignedInt(version) >>> SIGNIFICANT_SHIFT);
    }

    /**
     * Decodes the major version from an encoded version.
     *
     * @param version the encoded version
     * @return the major version
     */
    public static int major(final short version) {
        return (version >>> MAJOR_SHIFT) & MAJOR_MASK;
    }

    /**
     * Decodes the minor version from an encoded version.
     *
     * @param version the encoded version
     * @return the minor version
     */
    public static int minor(final short version) {
        return (version >>> MINOR_SHIFT) & MINOR_MASK;
    }

    /**
     * Creates a Minecraft version string from an encoded version.
     *
     * @param version the encoded version
     * @return a string representation
     */
    public static String asString(final short version) {
        final int significant = significant(version);
        final int major = major(version);
        final int minor = minor(version);
        return appropriateFormat(version).formatted(significant, major, minor);
    }

    /**
     * Converts a Minecraft version string to an encoded version.
     *
     * @param version the minecraft version string
     * @return an encoded version
     * @throws NumberFormatException if the version string is invalid
     */
    public static short fromString(final String version) throws NumberFormatException {
        if (version.contains("w")) {
            throw new UnsupportedOperationException("Legacy snapshots are unsupported");
        }

        // modern snapshot ex: 28.1-snapshot-14
        if (version.contains(SNAPSHOT_TELLTALE)) {
            return versionFromMCSnapshot(version);
        }

        return versionFromMCVersion(version);
    }

    private static short versionFromMCVersion(final String version) throws NumberFormatException {
        // 1.21.8 1.8.8 26.3.1
        final String[] versionParts = version.split("\\.");
        if (versionParts.length < 2) {
            return 0;
        }

        final int first  = Integer.parseInt(versionParts[0]);
        final int second = Integer.parseInt(versionParts[1]);
        if (versionParts.length == 3) {
            final int third = Integer.parseInt(versionParts[2]);
            return encode(first, second, third);
        } else if (first == 1) {
            return v1xy(second, 0);
        } else if (first < 26) {
            return v1xy(first, second);
        } else {
            return encode(first, second, 0);
        }
    }

    private static short versionFromMCSnapshot(final String version) throws NumberFormatException {
        final String[] parts = version.split(SNAPSHOT_TELLTALE);
        final String[] yearDrop = parts[0].split("\\.");
        if (yearDrop.length != 2) {
            return 0;
        }

        final int year = Integer.parseInt(yearDrop[0]);
        final int drop = Integer.parseInt(yearDrop[1]);
        final int patch = Integer.parseInt(parts[1]);
        return MinecraftVersionEncoding.snapshot(year, drop, patch);
    }

    private static String appropriateFormat(final short version) {
        return isSnapshot(version) ? SNAPSHOT_FORMAT : TRADITIONAL_FORMAT;
    }
}
