package me.samsuik.sakura.configuration.local;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A container for configuration values.
 */
@NullMarked
public sealed class ConfigurationContainer implements Container<ConfigurableKey<?>, Object> {
    private final IdentityHashMap<ConfigurableKey<?>, Object> values = new IdentityHashMap<>();

    public static SealedConfigurationContainer sealedContainer(final Object... contents) {
        if (contents.length % 2 != 0) {
            throw new IllegalArgumentException("Expected an even number of contents, got " + contents.length);
        }
        final IdentityHashMap<ConfigurableKey<?>, Object> values = new IdentityHashMap<>();
        for (int index = 0; index < contents.length; index += 2) {
            final Object key = contents[index];
            final Object value = contents[index + 1];
            if (!(key instanceof ConfigurableKey<?> configurableKey)) {
                throw new IllegalArgumentException("Key at index " + index + " must be of type ConfigurableKey");
            }
            values.put(configurableKey, configurableKey.validate(value));
        }
        return new SealedConfigurationContainer(values);
    }

    private ConfigurationContainer(final IdentityHashMap<ConfigurableKey<?>, Object> values) {
        this.values.putAll(values);
    }

    public ConfigurationContainer() {}

    public <V> @Nullable V set(final ConfigurableKey<V> key, final V value) {
        Preconditions.checkNotNull(value, "Value cannot be null");
        return key.conform(this.values.put(key, value));
    }

    public <V> @Nullable V remove(final ConfigurableKey<V> key) {
        return key.conform(this.values.remove(key));
    }

    public final <V> @Nullable V get(final ConfigurableKey<V> key) {
        return key.conform(this.values.get(key));
    }

    public final <V> Optional<V> getOptional(final ConfigurableKey<V> key) {
        return Optional.ofNullable(this.get(key));
    }

    @ApiStatus.Internal
    public final void fillAbsentValues(final ConfigurationContainer container) {
        for (final Map.Entry<ConfigurableKey<?>, Object> entry : container.values.entrySet()) {
            this.values.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        this.values.clear();
    }

    @Override
    public final Map<ConfigurableKey<?>, Object> contents() {
        return Map.copyOf(this.values);
    }

    public final ConfigurationContainer open() {
        return this instanceof SealedConfigurationContainer
            ? new ConfigurationContainer(this.values)
            : this;
    }

    public final SealedConfigurationContainer seal() {
        return this instanceof SealedConfigurationContainer sealed
            ? sealed
            : new SealedConfigurationContainer(this.values);
    }

    public static final class SealedConfigurationContainer extends ConfigurationContainer {
        private SealedConfigurationContainer(final IdentityHashMap<ConfigurableKey<?>, Object> values) {
            super(values);
        }

        @Override
        public <V> V set(final ConfigurableKey<V> key, final V value) {
            throw new UnsupportedOperationException("Container is sealed");
        }

        @Override
        public <V> V remove(final ConfigurableKey<V> key) {
            throw new UnsupportedOperationException("Container is sealed");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Container is sealed");
        }
    }
}
