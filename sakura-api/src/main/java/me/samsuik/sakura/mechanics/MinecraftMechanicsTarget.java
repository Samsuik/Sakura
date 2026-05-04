package me.samsuik.sakura.mechanics;

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

    public boolean isVanilla() {
        return this.serverType == ServerType.VANILLA;
    }

    public boolean isPaperOrDerivative() {
        return this.serverType == ServerType.PAPER || this.serverType == ServerType.SAKE;
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
            default -> MinecraftVersionEncoding.fromString(version);
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

    @Override
    public String toString() {
        return MechanicVersion.name(this.mechanicVersion) + "+" + ServerType.name(this.serverType);
    }
}
