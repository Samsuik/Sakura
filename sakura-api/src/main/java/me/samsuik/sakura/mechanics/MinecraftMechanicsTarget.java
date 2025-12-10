package me.samsuik.sakura.mechanics;

import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;

/**
 * The targeted Minecraft version and server type for cannon mechanics.
 */
@NullMarked
public record MinecraftMechanicsTarget(short mechanicVersion, byte serverType) {
    private static final MinecraftMechanicsTarget LATEST = new MinecraftMechanicsTarget(MechanicVersion.LATEST, ServerType.PAPER);
    private static final MinecraftMechanicsTarget LEGACY = new MinecraftMechanicsTarget(MechanicVersion.v1_8_2, ServerType.PAPER);

    public boolean isServerType(final byte type) {
        return this.serverType == type;
    }

    public boolean is(final short version) {
        return this.mechanicVersion == version;
    }

    public boolean before(final short version) {
        return this.mechanicVersion < version;
    }

    public boolean after(final short version) {
        return this.mechanicVersion > version;
    }

    public boolean atLeast(final short version) {
        return this.mechanicVersion >= version;
    }

    public boolean atMost(final short version) {
        return this.mechanicVersion <= version;
    }

    public boolean between(final short minVersion, final short maxVersion) {
        return this.mechanicVersion >= minVersion && this.mechanicVersion < maxVersion;
    }

    public boolean betweenInclusive(final short minVersion, final short maxVersion) {
        return this.mechanicVersion >= minVersion && this.mechanicVersion <= maxVersion;
    }

    public boolean isLegacy() {
        return this.mechanicVersion == MechanicVersion.LEGACY;
    }

    public boolean isSnapshot() {
        return MinecraftVersionEncoding.isSnapshot(this.mechanicVersion);
    }

    public static MinecraftMechanicsTarget latest() {
        return LATEST;
    }

    public static MinecraftMechanicsTarget legacy() {
        return LEGACY;
    }

    public static MinecraftMechanicsTarget vanilla(final short mechanicVersion) {
        return new MinecraftMechanicsTarget(mechanicVersion, ServerType.VANILLA);
    }

    public static MinecraftMechanicsTarget spigot(final short mechanicVersion) {
        return new MinecraftMechanicsTarget(mechanicVersion, ServerType.SPIGOT);
    }

    public static MinecraftMechanicsTarget paper(final short mechanicVersion) {
        return new MinecraftMechanicsTarget(mechanicVersion, ServerType.PAPER);
    }

    public static MinecraftMechanicsTarget sake(final short mechanicVersion) {
        return new MinecraftMechanicsTarget(mechanicVersion, ServerType.SAKE);
    }

    public static MinecraftMechanicsTarget fromString(final String target) throws NumberFormatException {
        final String[] parts = target.split("\\+");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Could not create a mechanics target from (" + target + ")");
        }

        final String version = parts[0];
        final short mechanicVersion = switch (version) {
            case "latest" -> MechanicVersion.LATEST;
            case "legacy" -> MechanicVersion.LEGACY;
            default -> isMinecraftSnapshot(version)
                ? versionFromMCSnapshot(version)
                : versionFromMCVersion(version);
        };

        final String serverPart = parts.length == 2 ? parts[1] : "";
        final byte serverType = switch (serverPart.toLowerCase(Locale.ENGLISH)) {
            case "vanilla" -> ServerType.VANILLA;
            case "spigot"  -> ServerType.SPIGOT;
            case "sake"    -> ServerType.SAKE;
            default        -> ServerType.PAPER;
        };

        return new MinecraftMechanicsTarget(mechanicVersion, serverType);
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
            return MinecraftVersionEncoding.encode(first, second, third);
        } else if (first == 1) {
            return MinecraftVersionEncoding.v1xy(second, 0);
        } else if (first < 26) {
            return MinecraftVersionEncoding.v1xy(first, second);
        } else {
            return MinecraftVersionEncoding.encode(first, second, 0);
        }
    }

    private static short versionFromMCSnapshot(final String version) throws NumberFormatException {
        // legacy snapshot ex: 19w43a, 22w14
        if (version.contains("w")) {
            final String[] parts = version.split("w");
            if (parts.length != 2) {
                return 0;
            }

            // clean up the revision
            final String revisionPart = parts[1];
            final int lastIndex = revisionPart.length() - 1;
            final char ending = revisionPart.charAt(lastIndex);
            final String cleanRevision = NumberUtils.isDigits(String.valueOf(ending))
                ? revisionPart
                : revisionPart.substring(0, lastIndex);

            final int release = Integer.parseInt(parts[0]);
            final int revision = Integer.parseInt(cleanRevision);
            return MinecraftVersionEncoding.smr(release, revision);
        }

        // modern snapshot ex: 28.1-snapshot-14
        if (version.contains("-snapshot-")) {
            final String[] parts = version.split("-snapshot-");
            final String[] yearDrop = parts[0].split("\\.");
            if (yearDrop.length != 2) {
                return 0;
            }

            final int year = Integer.parseInt(yearDrop[0]);
            final int drop = Integer.parseInt(yearDrop[1]);
            final int patch = Integer.parseInt(parts[1]);
            return MinecraftVersionEncoding.snapshot(year, drop, patch);
        }

        return 0;
    }

    private static boolean isMinecraftSnapshot(final String version) {
        return version.contains("w") || version.contains("-snapshot-");
    }

    @Override
    public String toString() {
        return MechanicVersion.name(this.mechanicVersion) + "+" + ServerType.name(this.serverType);
    }
}
