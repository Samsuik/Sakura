package me.samsuik.sakura.explosion;

import ca.spottedleaf.moonrise.common.util.WorldUtil;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class SpecialisedExplosion<T extends Entity> extends ServerExplosion {
    private static final double ENTITY_DISPATCH_DISTANCE = Math.pow(32.0, 2.0); 

    protected final T cause; // preferred over source
    private Vec3 impactPosition;
    protected final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final Consumer<SpecialisedExplosion<T>> applyEffects;

    public SpecialisedExplosion(ServerLevel level, T entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, Vec3 center, float power, boolean createFire, BlockInteraction destructionType, Consumer<SpecialisedExplosion<T>> applyEffects) {
        super(level, entity, damageSource, behavior, center, power, createFire, destructionType);
        this.cause = entity;
        this.impactPosition = center;
        this.applyEffects = applyEffects;
    }

    protected double getExplosionOffset() {
        return (double) this.cause.getBbHeight() * 0.0625D;
    }

    protected abstract int getExplosionCount();

    protected abstract void startExplosion();

    @Override
    public final void explode() {
        this.createBlockCache();
        this.startExplosion(); // search for blocks, impact entities, finalise if necessary
        this.clearBlockCache();
    }

    protected final boolean requiresImpactEntities(List<BlockPos> blocks, Vec3 center) {
        if (this.impactPosition.distanceToSqr(center) > ENTITY_DISPATCH_DISTANCE) {
            this.impactPosition = center;
            return true;
        }
        return !blocks.isEmpty();
    }

    protected final boolean finalizeExplosionAndParticles(List<BlockPos> blocks) {
        this.wasCanceled = false;
        List<BlockPos> explodedPositions = new ObjectArrayList<>(blocks);
        this.interactWithBlocks(explodedPositions);

        if (!this.wasCanceled) {
            this.applyEffects.accept(this);
            this.getHitPlayers().clear();
        }

        return !explodedPositions.isEmpty() && !this.wasCanceled;
    }

    protected void postExplosion(List<BlockPos> foundBlocks, boolean destroyedBlocks) {
        // optimisation: Keep the block cache across explosions and invalidate any found blocks.
        // This can help reduce block retrievals while block searching when there's a durable block,
        // and when ray tracing for obstructions. This is disabled by default because plugins can
        // change blocks in the explosion event.
        if (this.level().sakuraConfig().cannons.explosion.useBlockCacheAcrossExplosions && !foundBlocks.isEmpty() && !destroyedBlocks) {
            this.markBlocksInCacheAsExplodable(foundBlocks);
        } else {
            super.blockCache.clear();
        }

        Arrays.fill(this.directMappedBlockCache, null);
    }

    protected final void recalculateExplosionPosition() {
        this.recalculateExplosionPosition(this.cause);
    }

    protected final void recalculateExplosionPosition(T entity) {
        double x = entity.getX();
        double y = entity.getY() + this.getExplosionOffset();
        double z = entity.getZ();
        this.center = new Vec3(x, y, z);
    }

    protected final void forEachEntitySliceInBounds(AABB bb, Consumer<Entity[]> sliceConsumer) {
        int minSection = WorldUtil.getMinSection(this.level());
        int maxSection = WorldUtil.getMaxSection(this.level());

        int minChunkX = Mth.floor(bb.minX) >> 4;
        int minChunkY = Mth.clamp(Mth.floor(bb.minY) >> 4, minSection, maxSection);
        int minChunkZ = Mth.floor(bb.minZ) >> 4;
        int maxChunkX = Mth.floor(bb.maxX) >> 4;
        int maxChunkY = Mth.clamp(Mth.floor(bb.maxY) >> 4, minSection, maxSection);
        int maxChunkZ = Mth.floor(bb.maxZ) >> 4;

        EntityLookup entityLookup = this.level().moonrise$getEntityLookup();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                ChunkEntitySlices chunk = entityLookup.getChunk(chunkX, chunkZ);

                if (chunk == null) {
                    continue;
                }

                for (int chunkY = minChunkY; chunkY <= maxChunkY; ++chunkY) {
                    sliceConsumer.accept(chunk.getSectionEntities(chunkY));
                }
            }
        }
    }

    protected final void impactEntitiesFromPosition(Entity[] entities, Vec3 position, int potential, double radius) {
        for (int i = 0; i < entities.length; ++i) {
            Entity entity = entities[i];
            if (entity == null) break;

            if (entity != this.source && !entity.ignoreExplosion(this)) {
                this.impactEntity(entity, position, potential, radius);
            }

            if (entities[i] != entity) {
                i--;
            }
        }
    }

    protected final void impactEntity(Entity entity, Vec3 pos, int potential, double radius) {
        if (this.excludeSourceFromDamage && entity == this.source) {
            return; // for paper api
        }
        if (entity.isPrimedTNT || entity.isFallingBlock) {
            this.impactCannonEntity(entity, pos, potential, radius);
        } else {
            for (int i = 0; i < potential; ++i) {
                super.impactEntity((float) radius, entity);
            }
        }
    }

    protected final void impactCannonEntity(Entity entity, Vec3 pos, int potential, double radius) {
        double distanceFromBottom = Math.sqrt(entity.distanceToSqr(pos)) / radius;

        if (distanceFromBottom <= 1.0) {
            double x = entity.getX() - pos.x;
            double y = entity.getEyeY() - pos.y; // Sakura - configure cannon physics
            double z = entity.getZ() - pos.z;
            double distance = Math.sqrt(x * x + y * y + z * z);
            // Sakura start - configure cannon physics
            if (this.physics.before(1_17_0)) {
                distanceFromBottom = (float) distanceFromBottom;
                distance = (float) distance;
            }
            // Sakura end - configure cannon physics

            if (distance != 0.0D) {
                x /= distance;
                y /= distance;
                z /= distance;
                double density = this.getBlockDensity(pos, entity); // Paper - Optimize explosions
                double exposure = (1.0D - distanceFromBottom) * density;

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

    protected final void applyEntityVelocity(Entity entity, double x, double y, double z, int potential) {
        Vec3 movement = entity.getDeltaMovement();

        double moveX = movement.x();
        double moveY = movement.y();
        double moveZ = movement.z();

        for (int i = 0; i < potential; ++i) {
            moveX += x;
            moveY += y;
            moveZ += z;
        }

        entity.setDeltaMovement(moveX, moveY, moveZ);
        entity.hasImpulse = true;
    }
}
