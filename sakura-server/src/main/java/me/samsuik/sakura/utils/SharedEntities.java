package me.samsuik.sakura.utils;

import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

@NullMarked
public final class SharedEntities {
    private final ObjectArrayList<Entity> entities = new ObjectArrayList<>();
    private final AABB sharedBounds;
    private long lastTick;

    public SharedEntities(final AABB bounds) {
        this.sharedBounds = bounds;
    }

    public List<Entity> getEntities(final Level level, final AABB bounds, final @Nullable Predicate<? super Entity> selector) {
        final long gameTime = level.getGameTime();
        if (gameTime != lastTick) {
            this.updateSharedEntities(level);
            this.lastTick = gameTime;
        }

        final List<Entity> ret = new ObjectArrayList<>();
        for (final Entity entity : this.entities) {
            if (entity.getBoundingBox().intersects(bounds) && (selector == null || selector.test(entity))) {
                ret.add(entity);
            }
        }

        return ret;
    }

    private void updateSharedEntities(final Level level) {
        this.entities.clear();
        ((ChunkSystemLevel) level).moonrise$getEntityLookup().getEntities(
            (Entity) null, this.sharedBounds, this.entities, null
        );
    }
}
