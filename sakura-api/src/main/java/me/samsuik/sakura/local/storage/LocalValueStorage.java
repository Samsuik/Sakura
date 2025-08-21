package me.samsuik.sakura.local.storage;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.samsuik.sakura.local.LocalValueKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

@NullMarked
@Deprecated(forRemoval = true)
@SuppressWarnings("unchecked")
public final class LocalValueStorage {
    private final Map<LocalValueKey<?>, Object> map = new Object2ObjectOpenHashMap<>();

    public <T> void set(LocalValueKey<T> key, T insert) {
        this.map.put(key, insert);
    }

    public void remove(LocalValueKey<?> key) {
        this.map.remove(key);
    }

    public <T> Optional<T> get(LocalValueKey<T> key) {
        T value = (T) this.map.get(key);
        return Optional.ofNullable(value);
    }

    public <T> T getOrDefault(LocalValueKey<T> key, T def) {
        return (T) this.map.getOrDefault(key, def);
    }

    public boolean exists(LocalValueKey<?> key) {
        return this.map.containsKey(key);
    }

    @Nullable
    public <T> T value(LocalValueKey<T> key) {
        return (T) this.map.get(key);
    }

    public <T> T value(LocalValueKey<T> key, boolean returnDefault) {
        T val = (T) this.map.get(key);
        if (!returnDefault || val != null)
            return val;
        // update value
        this.set(key, val = key.defaultSupplier().get());
        return val;
    }
}
