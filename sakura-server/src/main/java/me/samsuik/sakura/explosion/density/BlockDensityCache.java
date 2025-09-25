package me.samsuik.sakura.explosion.density;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class BlockDensityCache {
    public static final float UNKNOWN_DENSITY = -1.0f;

    private final Object2FloatOpenHashMap<BlockDensityCacheKey> paperExactPosDensityCache = new Object2FloatOpenHashMap<>();
    private final Int2ObjectOpenHashMap<CachedBlockDensity> lenientDensityCache = new Int2ObjectOpenHashMap<>();
    private @Nullable BlockDensityCacheKey densityCacheKey;
    private @Nullable CachedBlockDensity cacheInUse;
    private int cacheKeyInUse;
    private boolean knownSource;
    private final Level level;

    public BlockDensityCache(final Level level) {
        this.level = level;
        this.paperExactPosDensityCache.defaultReturnValue(UNKNOWN_DENSITY);
    }

    public float getBlockDensity(final Vec3 explosionPos, final Entity entity) {
        final int lenientKey = BlockDensityCacheKey.getLenientKey(explosionPos, entity.blockPosition());
        final CachedBlockDensity cache = this.lenientDensityCache.get(lenientKey);

        // Check if the density is cached
        if (cache != null && cache.hasPosition(explosionPos, entity.getBoundingBox())) {
            return cache.blockDensity();
        }

        // Replicate the broken behaviour of optimize-explosions
        if (this.level.paperConfig().environment.optimizeExplosions) {
            final BlockDensityCacheKey cacheKey = new BlockDensityCacheKey(explosionPos, entity);
            final float blockDensity = this.paperExactPosDensityCache.getFloat(cacheKey);
            if (blockDensity != UNKNOWN_DENSITY) {
                return blockDensity;
            }
            this.densityCacheKey = cacheKey;
        }

        this.knownSource = cache != null && cache.complete() && cache.isExplosionPosition(explosionPos);
        this.cacheInUse = cache;
        this.cacheKeyInUse = lenientKey;
        return UNKNOWN_DENSITY;
    }

    public float getKnownDensity(final Vec3 point) {
        return this.knownSource && this.cacheInUse.isKnownPosition(point)
            ? this.cacheInUse.blockDensity()
            : UNKNOWN_DENSITY;
    }

    public void putDensity(final Vec3 explosionPos, final Entity entity, final float blockDensity) {
        final CachedBlockDensity cache = this.cacheInUse;
        if (cache == null || !cache.complete()) {
            this.lenientDensityCache.put(this.cacheKeyInUse, new CachedBlockDensity(explosionPos, entity, blockDensity));
        } else if (cache.blockDensity() == blockDensity) {
            cache.expand(explosionPos, entity);
        }

        if (this.level.paperConfig().environment.optimizeExplosions && this.densityCacheKey != null) {
            this.paperExactPosDensityCache.put(this.densityCacheKey, blockDensity);
        }
    }

    public void expire(final long tick) {
        this.invalidate();

        if (tick % 600 == 0) {
            // Trim everything down every 600 ticks
            this.paperExactPosDensityCache.trim(0);
            this.lenientDensityCache.trim(0);
        }
    }

    public void invalidate() {
        this.lenientDensityCache.clear();
    }
}
