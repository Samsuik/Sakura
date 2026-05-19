package me.samsuik.sakura.configuration.local;

import io.papermc.paper.math.Position;
import me.samsuik.sakura.configuration.local.ConfigurationContainer.SealedConfigurationContainer;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An accessor for local configuration containers.
 */
@NullMarked
public interface LocalConfigurationAccessor {
    default <V> void set(final BoundingBox area, final ConfigurationKey<V> key, final V value) {
        final ConfigurationContainer container = this.get(area);
        final ConfigurationContainer newContainer = container != null
            ? container.open()
            : new ConfigurationContainer();
        newContainer.set(key, value);
        this.set(area, newContainer.seal());
    }

    default void remove(final BoundingBox area, final ConfigurationKey<?> key) {
        final ConfigurationContainer container = this.get(area);
        if (container != null) {
            final ConfigurationContainer newContainer = container.open();
            newContainer.remove(key);
            this.set(area, newContainer.seal());
        }
    }

    default <T> @Nullable T get(final BoundingBox area, final ConfigurationKey<T> key) {
        final ConfigurationContainer container = this.get(area);
        return container == null ? null : container.get(key);
    }

    void set(final BoundingBox area, final SealedConfigurationContainer container);

    @Nullable SealedConfigurationContainer remove(final BoundingBox area);

    @Nullable SealedConfigurationContainer get(final BoundingBox area);

    default <T> @Nullable T getValue(final Vector vector, final ConfigurationKey<T> key) {
        final ConfigurationContainer container = this.getContainer(vector);
        return container != null ? container.get(key) : null;
    }

    default <T> @Nullable T getValue(final Position position, final ConfigurationKey<T> key) {
        final ConfigurationContainer container = this.getContainer(position);
        return container != null ? container.get(key) : null;
    }

    default @Nullable ConfigurationContainer getContainer(final Vector vector) {
        return this.getContainer(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    default @Nullable ConfigurationContainer getContainer(final Position position) {
        return this.getContainer(position.blockX(), position.blockY(), position.blockZ());
    }

    @Nullable ConfigurationContainer getContainer(final int x, final int y, final int z);

    default List<BoundingBox> getAreas(final Vector vector) {
        return this.getAreas(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    default List<BoundingBox> getAreas(final Position position) {
        return this.getAreas(position.blockX(), position.blockY(), position.blockZ());
    }

    List<BoundingBox> getAreas(final int x, final int y, final int z);
}
