package me.samsuik.sakura.redstone;

import org.jspecify.annotations.NullMarked;

/**
 * The redstone implementation to use.
 */
@NullMarked
public enum RedstoneImplementation {
    VANILLA("vanilla"),
    EIGENCRAFT("eigencraft"),
    ALTERNATE_CURRENT("alternate-current");

    private final String friendlyName;

    RedstoneImplementation(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public final String getFriendlyName() {
        return this.friendlyName;
    }
}
