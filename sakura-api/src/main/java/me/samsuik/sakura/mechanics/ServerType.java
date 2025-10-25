package me.samsuik.sakura.mechanics;

import org.jspecify.annotations.NullMarked;

/**
 * Types of server software that have different cannon mechanics.
 */
@NullMarked
public final class ServerType {
    public static final byte VANILLA = 0;
    public static final byte SPIGOT = 1;
    public static final byte PAPER = 2;
    public static final byte SAKE = 32;

    public static String name(final byte serverType) {
        return switch (serverType) {
            case 0 -> "vanilla";
            case 1 -> "spigot";
            case 2 -> "paper";
            case 32 -> "sake";
            default -> "unknown";
        };
    }
}
