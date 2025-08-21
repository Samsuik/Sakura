package me.samsuik.sakura.local;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
@Deprecated(forRemoval = true)
public record LocalValueKey<T>(NamespacedKey key, Supplier<T> defaultSupplier) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        LocalValueKey<?> that = (LocalValueKey<?>) o;
        return this.key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
}
