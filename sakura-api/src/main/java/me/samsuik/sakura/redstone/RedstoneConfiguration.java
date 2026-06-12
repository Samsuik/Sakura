package me.samsuik.sakura.redstone;

import org.jspecify.annotations.NullMarked;

/**
 * Configuration for redstone behaviour
 *
 * @param implementation the redstone implementation to use
 * @param cache          whether to cache redstone calculations
 */
@NullMarked
public record RedstoneConfiguration(RedstoneImplementation implementation, boolean cache) {
    @Deprecated(forRemoval = true)
    public static RedstoneConfiguration withImplementation(final RedstoneImplementation implementation) {
        return withoutCache(implementation);
    }

    public static RedstoneConfiguration withoutCache(final RedstoneImplementation implementation) {
        return new RedstoneConfiguration(implementation, false);
    }

    public static RedstoneConfiguration withCache(final RedstoneImplementation implementation) {
        return new RedstoneConfiguration(implementation, true);
    }
}
