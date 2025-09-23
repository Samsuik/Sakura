package me.samsuik.sakura.mechanics;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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

    public static @Nullable MinecraftMechanicsTarget fromString(final String target) throws NumberFormatException {
        // 1.21.8+paper 1.8.8+vanilla 12.2
        final String[] parts = target.split("\\+");
        final String serverPart = parts.length == 2 ? parts[1] : "";
        final byte serverType = switch (serverPart.toLowerCase(Locale.ENGLISH)) {
            case "vanilla" -> ServerType.VANILLA;
            case "spigot"  -> ServerType.SPIGOT;
            default        -> ServerType.PAPER;
        };

        if (parts.length == 0) {
            return null;
        }

        final String[] version = parts[0].split("\\.");
        if (version.length < 1) {
            return null;
        }

        final short mechanicVersion;
        if (version.length == 1) {
            mechanicVersion = switch (version[0]) {
                case "latest" -> MechanicVersion.LATEST;
                case "legacy" -> MechanicVersion.LEGACY;
                default -> 0;
            };
        } else {
            // 21.1 -> 1.21.1
            final int first  = Integer.parseInt(version[0]);
            final int second = Integer.parseInt(version[1]);
            if (version.length == 3) {
                final int third = Integer.parseInt(version[2]);
                mechanicVersion = MinecraftVersionEncoding.encode(first, second, third);
            } else {
                mechanicVersion = MinecraftVersionEncoding.v1xy(first, second);
            }
        }

        return new MinecraftMechanicsTarget(mechanicVersion, serverType);
    }
}
