package me.samsuik.sakura.configuration.local;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A key for a configurable value.
 */
@NullMarked
public record ConfigurationKey<T>(Key key, Class<T> expectedType) implements Keyed {
    public T validate(@Nullable final Object value) {
        final T casted = this.conform(value);
        if (casted == null) {
            throw new IllegalArgumentException("Value cannot be null for key " + this);
        }
        return casted;
    }

    public @Nullable T conform(@Nullable final Object value) {
        if (value == null) {
            return null;
        }
        if (!this.expectedType.isInstance(value)) {
            throw new IllegalArgumentException("Expected type " + this.expectedType.getName() + " but got " + value.getClass().getName());
        }
        return this.expectedType.cast(value);
    }
}
