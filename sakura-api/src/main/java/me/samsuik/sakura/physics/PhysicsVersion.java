package me.samsuik.sakura.physics;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum PhysicsVersion {
    LEGACY("legacy", 1_0_0),  // replicates patched 1.8.8 paper mechanics
    v1_8_2("1.8.2", 1_8_2),   // vanilla mechanics
    v1_9("1.9", 1_9_0),
    v1_10("1.10", 1_10_0),
    v1_11("1.11", 1_11_0),
    v1_12("1.12", 1_12_0),
    v1_13("1.13", 1_13_0),
    v1_14("1.14", 1_14_0),
    v1_16("1.16", 1_16_0),
    v1_17("1.17", 1_17_0),
    v1_18_2("1.18.2", 1_18_2),
    v1_19_3("1.19.3", 1_19_3),
    v1_20("1.20", 1_20_0),
    v1_21_2("1.21.2", 1_21_2),
    v1_21_5("1.21.5", 1_21_5),
    LATEST("latest", 9_99_9); // latest version

    private final String friendlyName;
    private final int version;

    PhysicsVersion(String friendlyName, int version) {
        this.friendlyName = friendlyName;
        this.version = version;
    }

    public boolean isLegacy() {
        return this == LEGACY;
    }

    public boolean afterOrEqual(int version) {
        return this.version >= version;
    }

    public boolean before(int version) {
        return this.version < version;
    }

    public boolean is(int version) {
        return this.version == version;
    }

    public boolean isWithin(int min, int max) {
        return this.version >= min && this.version <= max;
    }

    public int getVersion() {
        return this.version;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public static PhysicsVersion from(String string) {
        int parsedVersion = Integer.MIN_VALUE;
        try {
            String versionString = string.replace(".", "");
            parsedVersion = Integer.parseInt(versionString);
        } catch (NumberFormatException nfe) {
            // ignored
        }
        for (PhysicsVersion ver : values()) {
            if (ver.name().equalsIgnoreCase(string) || ver.getFriendlyName().equalsIgnoreCase(string) || ver.is(parsedVersion)) {
                return ver;
            }
        }
        return LATEST;
    }
}
