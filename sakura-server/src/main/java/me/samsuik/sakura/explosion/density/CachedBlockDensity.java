package me.samsuik.sakura.explosion.density;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CachedBlockDensity {
    private AABB source;
    private AABB entity;
    private final float blockDensity;
    private final boolean complete;

    public CachedBlockDensity(final Vec3 explosionPos, final Entity entity, final float blockDensity) {
        this.source = new AABB(explosionPos, explosionPos);
        this.entity = entity.getBoundingBox();
        this.blockDensity = blockDensity;
        this.complete = blockDensity == 0.0f || blockDensity == 1.0f;
    }

    public float blockDensity() {
        return this.blockDensity;
    }

    public boolean complete() {
        return this.complete;
    }

    public boolean hasPosition(final Vec3 explosionPos, final AABB entityBoundingBox) {
        return this.isExplosionPosition(explosionPos) && this.entity.containsInclusive(entityBoundingBox);
    }

    public boolean isKnownPosition(final Vec3 point) {
        return this.entity.containsInclusive(point);
    }

    public boolean isExplosionPosition(final Vec3 explosionPos) {
        return this.source.containsInclusive(explosionPos);
    }

    public void expand(final Vec3 explosionPos, final Entity entity) {
        this.source = this.source.expand(explosionPos);
        this.entity = this.entity.minmax(entity.getBoundingBox());
    }
}
