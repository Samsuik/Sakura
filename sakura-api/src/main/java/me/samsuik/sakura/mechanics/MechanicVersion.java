package me.samsuik.sakura.mechanics;

/**
 * All post-1.8 Minecraft versions with changes to cannon mechanics.
 * <p>
 * Versions are encoded as shorts see {@link MinecraftVersionEncoding}.
 */
public final class MechanicVersion {
    public static final short LATEST = Short.MAX_VALUE;
    public static final short LEGACY = Short.MIN_VALUE;
    public static final short v1_8_2 = MinecraftVersionEncoding.v1xy(8, 2);
    public static final short v1_9 = MinecraftVersionEncoding.v1xy(9, 0);
    public static final short v1_10 = MinecraftVersionEncoding.v1xy(10, 0);
    public static final short v1_11 = MinecraftVersionEncoding.v1xy(11, 0);
    public static final short v1_12 = MinecraftVersionEncoding.v1xy(12, 0);
    public static final short v1_13 = MinecraftVersionEncoding.v1xy(13, 0);
    public static final short v1_14 = MinecraftVersionEncoding.v1xy(14, 0);
    public static final short v1_16 = MinecraftVersionEncoding.v1xy(16, 0);
    public static final short v1_17 = MinecraftVersionEncoding.v1xy(17, 0);
    public static final short v1_18_2 = MinecraftVersionEncoding.v1xy(18, 2);
    public static final short v1_19_3 = MinecraftVersionEncoding.v1xy(19, 3);
    public static final short v1_20 = MinecraftVersionEncoding.v1xy(20, 0);
    public static final short v1_20_2 = MinecraftVersionEncoding.v1xy(20, 2);
    public static final short v1_21_2 = MinecraftVersionEncoding.v1xy(21, 2);
    public static final short v1_21_5 = MinecraftVersionEncoding.v1xy(21, 5);
    public static final short v1_21_6 = MinecraftVersionEncoding.v1xy(21, 6);

    public static String name(final short version) {
        if (version == LATEST) {
            return "latest";
        } else if (version == LEGACY) {
            return "legacy";
        }

        final int significant = MinecraftVersionEncoding.significant(version);
        final int major = MinecraftVersionEncoding.major(version);
        final int minor = MinecraftVersionEncoding.minor(version);
        return String.format("%d.%d.%d", significant, major, minor);
    }
}
