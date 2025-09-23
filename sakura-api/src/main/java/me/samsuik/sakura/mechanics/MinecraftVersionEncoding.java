package me.samsuik.sakura.mechanics;

/**
 * Encode Minecraft versions into a short.
 */
public final class MinecraftVersionEncoding {
    private static final int SIGNIFICANT_SHIFT = Short.SIZE - 4;
    private static final int MAJOR_SHIFT = SIGNIFICANT_SHIFT - 6;
    private static final int MINOR_SHIFT = MAJOR_SHIFT - 6;

    /**
     * Encodes a 1.x.y Minecraft version into a short.
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
        encoded |= (short) (significant << SIGNIFICANT_SHIFT);
        encoded |= (short) (major << MAJOR_SHIFT);
        encoded |= (short) (minor << MINOR_SHIFT);
        return encoded;
    }

    /**
     * Decodes the significant version from an encoded version.
     *
     * @param version the encoded version
     * @return the significant version
     */
    public static int significant(final short version) {
        return (version >>> SIGNIFICANT_SHIFT) & 15;
    }

    /**
     * Decodes the major version from an encoded version.
     *
     * @param version the encoded version
     * @return the major version
     */
    public static int major(final short version) {
        return (version >>> MAJOR_SHIFT) & 63;
    }

    /**
     * Decodes the minor version from an encoded version.
     *
     * @param version the encoded version
     * @return the minor version
     */
    public static int minor(final short version) {
        return (version >>> MINOR_SHIFT) & 63;
    }
}
