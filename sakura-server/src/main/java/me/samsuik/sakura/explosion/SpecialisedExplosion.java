package me.samsuik.sakura.explosion;

import ca.spottedleaf.moonrise.common.util.WorldUtil;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import it.unimi.dsi.fastutil.objects.*;
import me.samsuik.sakura.mechanics.MechanicVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.function.Consumer;

@NullMarked
public abstract class SpecialisedExplosion<T extends Entity> extends ServerExplosion {
    private static final double ENTITY_DISPATCH_DISTANCE_SQR = 32.0 * 32.0;

    protected final T cause; // preferred over source
    private Vec3 dispatchPosition;
    private final List<Vec3> bufferedExplosions = new ObjectArrayList<>();
    private AABB bounds;
    private final Set<BlockPos> gameEvents = new ObjectOpenHashSet<>();
    private final Deque<ExplosionToSend> explosionsToSend = new ArrayDeque<>();

    public SpecialisedExplosion(
        final ServerLevel level,
        final T entity,
        final @Nullable DamageSource damageSource,
        final @Nullable ExplosionDamageCalculator behavior,
        final Vec3 center,
        final float power,
        final boolean createFire,
        final BlockInteraction destructionType
    ) {
        super(level, entity, damageSource, behavior, center, power, createFire, destructionType);
        this.cause = entity;
        this.dispatchPosition = center;
        this.bounds = new AABB(center, center);
    }

    public final Queue<ExplosionToSend> getExplosionsToSend() {
        return this.explosionsToSend;
    }

    protected double getExplosionOffset() {
        return 0.0;
    }

    protected abstract int handleExplosion();

    @Override
    public final int explode() {
        this.createBlockCache();
        final int blocksDestroyed = this.handleExplosion();
        this.clearBlockCache();
        return blocksDestroyed;
    }

    protected final List<BlockPos> collectBlocksAndImpactEntities(final boolean interactWithBlocks, final boolean dispatch) {
        final Vec3 center = this.center;
        if (interactWithBlocks && this.gameEvents.add(BlockPos.containing(center))) {
            this.level().gameEvent(this.cause, GameEvent.EXPLODE, center);
        }

        // Collect all the blocks to explode
        final List<BlockPos> blocksToExplode = interactWithBlocks
            ? this.calculateExplodedPositions()
            : List.of();

        // Buffer explosions to reduce the amount of calculations and improve locality
        this.bounds = this.bounds.expand(center);
        this.bufferedExplosions.add(center);

        // Dispatch the buffered explosions
        if (dispatch || this.needToDispatchEntities(blocksToExplode, center)) {
            this.locateAndImpactEntitiesInBounds(this.bounds, this.bufferedExplosions);
            this.bounds = new AABB(center, center);
            this.bufferedExplosions.clear();
            this.gameEvents.clear();
        }

        return blocksToExplode;
    }

    protected final boolean needToDispatchEntities(final List<BlockPos> blocksToBlow, final Vec3 center) {
        if (this.dispatchPosition.distanceToSqr(center) > ENTITY_DISPATCH_DISTANCE_SQR) {
            this.dispatchPosition = center;
            return true;
        }

        return !blocksToBlow.isEmpty();
    }

    protected final int finalizeExplosionAndParticles(final List<BlockPos> blocksToBlow, final boolean lastCycle) {
        this.wasCanceled = false;
        final List<BlockPos> explodedPositions = new ObjectArrayList<>(blocksToBlow);
        this.interactWithBlocks(explodedPositions);

        if (!this.wasCanceled && !lastCycle) {
            // Packets are sent after the explosion
            this.explosionsToSend.add(new ExplosionToSend(this.center, explodedPositions.size()));
        }

        return this.wasCanceled ? 0 : explodedPositions.size();
    }

    protected void nextExplosion(final List<BlockPos> foundBlocks, final boolean destroyedBlocks) {
        // Reuse the block cache between explosions. This can help a lot when searching for blocks and raytracing.
        // This is disabled by default as it's incompatible with plugins that modify blocks in the explosion event.
        if (this.level().sakuraConfig().cannons.explosion.reuseBlockCacheAcrossExplosions && !foundBlocks.isEmpty() && !destroyedBlocks) {
            this.markBlocksInCacheAsExplodable(foundBlocks);
        } else {
            super.blockCache.clear();
        }

        Arrays.fill(this.directMappedBlockCache, null);
    }

    protected final void recalculateExplosionPosition() {
        final double x = this.cause.getX();
        final double y = this.cause.getY() + this.getExplosionOffset();
        final double z = this.cause.getZ();
        this.center = new Vec3(x, y, z);
    }

    protected final void locateAndImpactEntitiesInBounds(final AABB bounds, final List<Vec3> explosions) {
        final double radius = this.radius() * 2.0f;
        final double change = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        final double maxDistanceSqr = Math.pow(radius + change, 2.0);
        final boolean positionChanged = change != 0.0;

        this.forEachEntitySliceInBounds(bounds.inflate(radius), entities -> {
            if (positionChanged) {
                this.impactEntitiesMoving(entities, explosions, bounds.getCenter(), radius, maxDistanceSqr);
            } else {
                this.impactEntitiesFromPosition(entities, explosions.getFirst(), explosions.size(), radius);
            }
        });
    }

    protected final void impactEntitiesMoving(
        final @Nullable Entity[] entities,
        final List<Vec3> explosions,
        final Vec3 center,
        final double radius,
        final double maxDistanceSqr
    ) {
        for (int index = 0; index < entities.length; ++index) {
            final Entity entity = entities[index];
            if (entity == null) break; // end of the entity slice

            // Check if the entity is in range
            if (entity.distanceToSqr(center.x, center.y, center.z) > maxDistanceSqr) {
                continue;
            }

            //noinspection ForLoopReplaceableByForEach
            for (int count = 0; count < explosions.size(); count++) {
                this.impactEntity(entity, explosions.get(count), 1, radius);
            }

            // Entities can be removed from the world mid-explosion.
            if (entities[index] != entity) {
                index--;
            }
        }
    }

    protected final void impactEntitiesFromPosition(
        final @Nullable Entity[] entities,
        final Vec3 position,
        final int potential,
        final double radius
    ) {
        for (int index = 0; index < entities.length; ++index) {
            final Entity entity = entities[index];
            if (entity == null) break; // end of the entity slice

            this.impactEntity(entity, position, potential, radius);

            // Entities can be removed from the world mid-explosion.
            if (entities[index] != entity) {
                index--;
            }
        }
    }

    protected final void impactEntity(final Entity entity, final Vec3 pos, final int potential, final double radius) {
        if (this.excludeSourceFromDamage && this.source == entity || entity.ignoreExplosion(this)) {
            return; // Make sure the entity can be affected by explosions.
        }
        if (entity.isPrimedTNT || entity.isFallingBlock) {
            this.impactCannonEntity(entity, pos, potential, radius);
        } else {
            for (int i = 0; i < potential; ++i) {
                super.impactEntity((float) radius, entity);
            }
        }
    }

    protected final void impactCannonEntity(final Entity entity, final Vec3 pos, final int potential, final double radius) {
        double distanceFromBottom = Math.sqrt(entity.distanceToSqr(pos)) / radius;
        if (distanceFromBottom <= 1.0) {
            double x = entity.getX() - pos.x;
            double y = entity.getEyeY() - pos.y;
            double z = entity.getZ() - pos.z;
            double distance = Math.sqrt(x * x + y * y + z * z);

            if (this.mechanicsTarget.before(MechanicVersion.v1_17)) {
                distanceFromBottom = (float) distanceFromBottom;
                distance = (float) distance;
            }

            if (distance >= 1.0e-5 || distance != 0.0 && this.mechanicsTarget.before(MechanicVersion.v1_21_9)) {
                x /= distance;
                y /= distance;
                z /= distance;
                final double density = this.getBlockDensity(pos, entity); // Paper - Optimize explosions
                final double exposure = (1.0D - distanceFromBottom) * density;

                if (exposure == 0.0) {
                    return;
                }

                x *= exposure;
                y *= exposure;
                z *= exposure;

                this.applyEntityVelocity(entity, x, y, z, potential);
            }
        }
    }

    protected final void applyEntityVelocity(final Entity entity, final double x, final double y, final double z, final int potential) {
        final Vec3 movement = entity.getDeltaMovement();
        double moveX = movement.x();
        double moveY = movement.y();
        double moveZ = movement.z();

        for (int count = 0; count < potential; ++count) {
            moveX += x;
            moveY += y;
            moveZ += z;
        }

        entity.setDeltaMovement(moveX, moveY, moveZ);
        entity.hasImpulse = true;
    }

    protected final void forEachEntitySliceInBounds(final AABB bb, final Consumer<Entity[]> sliceConsumer) {
        final int minSection = WorldUtil.getMinSection(this.level());
        final int maxSection = WorldUtil.getMaxSection(this.level());

        final int minChunkX = Mth.floor(bb.minX) >> 4;
        final int minChunkY = Mth.clamp(Mth.floor(bb.minY) >> 4, minSection, maxSection);
        final int minChunkZ = Mth.floor(bb.minZ) >> 4;
        final int maxChunkX = Mth.floor(bb.maxX) >> 4;
        final int maxChunkY = Mth.clamp(Mth.floor(bb.maxY) >> 4, minSection, maxSection);
        final int maxChunkZ = Mth.floor(bb.maxZ) >> 4;

        final EntityLookup entityLookup = this.level().moonrise$getEntityLookup();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                final ChunkEntitySlices chunk = entityLookup.getChunk(chunkX, chunkZ);

                if (chunk == null) {
                    continue;
                }

                for (int chunkY = minChunkY; chunkY <= maxChunkY; ++chunkY) {
                    sliceConsumer.accept(chunk.getSectionEntities(chunkY));
                }
            }
        }
    }
}
