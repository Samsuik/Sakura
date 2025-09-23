package me.samsuik.sakura.configuration.local;

import me.samsuik.sakura.explosion.durable.DurableMaterialsContainer.SealedDurableMaterialsContainer;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import me.samsuik.sakura.redstone.RedstoneConfiguration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A key for a configurable value.
 */
@NullMarked
public record ConfigurableKey<T>(Class<T> expectedType) {
    public static final ConfigurableKey<MinecraftMechanicsTarget> MECHANICS_TARGET = new ConfigurableKey<>(MinecraftMechanicsTarget.class);
    public static final ConfigurableKey<SealedDurableMaterialsContainer> DURABLE_MATERIALS = new ConfigurableKey<>(SealedDurableMaterialsContainer.class);
    public static final ConfigurableKey<RedstoneConfiguration> REDSTONE_BEHAVIOUR = new ConfigurableKey<>(RedstoneConfiguration.class);
    public static final ConfigurableKey<Boolean> CONSISTENT_EXPLOSION_RADIUS = new ConfigurableKey<>(Boolean.class);
    public static final ConfigurableKey<Integer> LAVA_FLOW_SPEED = new ConfigurableKey<>(Integer.class);

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
