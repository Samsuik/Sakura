package me.samsuik.sakura.redstone;

import org.jspecify.annotations.NullMarked;

/**
 * Configuration for redstone behavior
 *
 * @param implementation the redstone implementation to use
 * @param cache          whether to cache redstone calculations
 */
@NullMarked
public record RedstoneConfiguration(RedstoneImplementation implementation, boolean cache) {
    public static RedstoneConfiguration withImplementation(final RedstoneImplementation implementation) {
        return new RedstoneConfiguration(implementation, false);
    }
}
