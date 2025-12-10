package me.samsuik.sakura.mechanics;

/**
 * Encode Minecraft versions into a short.
 */
public final class MinecraftVersionEncoding {
    private static final int SNAPSHOT_SHIFT = Short.SIZE - 1;
    private static final int SIGNIFICANT_SHIFT = SNAPSHOT_SHIFT - 5;
    private static final int MAJOR_SHIFT = SIGNIFICANT_SHIFT - 5;
    private static final int MINOR_SHIFT = MAJOR_SHIFT - 5;

    private static final int GAME_DROP_SHIFT = SIGNIFICANT_SHIFT - 4;
    private static final int SNAPSHOT_BIT = 1 << SNAPSHOT_SHIFT;

    /**
     * Encodes a Minecraft snapshot version into a short.
     *
     * @param year  the year
     * @param drop  the game drop
     * @param patch the patch
     * @return the encoded snapshot version as a short
     */
    public static short snapshot(final int year, final int drop, final int patch) {
        return smr(year, drop << GAME_DROP_SHIFT | patch);
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
     * Encodes a legacy x(w)y Minecraft snapshot into a short.
     *
     * @param major    the major version (x)
     * @param revision the revision      (y)
     * @return the encoded snapshot version as a short
     */
    public static short smr(final int major, final int revision) {
        short encoded = 0;
        encoded |= (short) SNAPSHOT_BIT;
        encoded |= (short) (major << SIGNIFICANT_SHIFT);
        encoded |= (short) revision;
        return encoded;
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
        encoded |= (short) (significant << SIGNIFICANT_SHIFT);
        encoded |= (short) (major << MAJOR_SHIFT);
        encoded |= (short) minor;
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
     * Whether an encoded version is a modern Minecraft snapshot.
     *
     * @param version the encoded version
     * @return if the version is a modern snapshot
     */
    public static boolean isModernSnapshot(final short version) {
        return isSnapshot(version) && gameDrop(version) != 0;
    }

    /**
     * Decodes the snapshot revision from an encoded version.
     *
     * @param version the encoded version
     * @return the snapshot revision
     */
    public static int revision(final short version) {
        return version & ((1 << SIGNIFICANT_SHIFT) - 1);
    }

    /**
     * Decodes the game drop from an encoded version.
     *
     * @param version the encoded version
     * @return the game drop
     */
    public static int gameDrop(final short version) {
        return (revision(version) >>> GAME_DROP_SHIFT) & 15;
    }

    /**
     * Decodes the patch from an encoded version.
     *
     * @param version the encoded version
     * @return the patch
     */
    public static int patch(final short version) {
        return revision(version) & 63;
    }

    /**
     * Decodes the significant version from an encoded version.
     *
     * @param version the encoded version
     * @return the significant version
     */
    public static int significant(final short version) {
        return (version >>> SIGNIFICANT_SHIFT) & 31;
    }

    /**
     * Decodes the major version from an encoded version.
     *
     * @param version the encoded version
     * @return the major version
     */
    public static int major(final short version) {
        return (version >>> MAJOR_SHIFT) & 31;
    }

    /**
     * Decodes the minor version from an encoded version.
     *
     * @param version the encoded version
     * @return the minor version
     */
    public static int minor(final short version) {
        return (version >>> MINOR_SHIFT) & 31;
    }
}
