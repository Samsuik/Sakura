package me.samsuik.sakura.explosion;

import ca.spottedleaf.moonrise.common.list.IteratorSafeOrderedReferenceSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.samsuik.sakura.entity.EntityState;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.entity.merge.MergeableEntity;
import me.samsuik.sakura.mechanics.MechanicVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Consumer;

@NullMarked
public final class TntExplosion extends SpecialisedExplosion<PrimedTnt> {
    private static final int ALL_DIRECTIONS = 0b111;
    private static final int FOUND_ALL_BLOCKS = ALL_DIRECTIONS + 12;

    private final Vec3 originalPosition;
    private final List<Vec3> explosions = new ObjectArrayList<>();
    private AABB bounds;
    private int swinging = 0;
    private boolean moved = false;

    public TntExplosion(
        final ServerLevel level,
        final PrimedTnt tnt,
        final @Nullable DamageSource damageSource,
        final @Nullable ExplosionDamageCalculator behavior,
        final Vec3 center,
        final float power,
        final boolean createFire,
        final BlockInteraction destructionType,
        final Consumer<SpecialisedExplosion<PrimedTnt>> applyEffects
    ) {
        super(level, tnt, damageSource, behavior, center, power, createFire, destructionType, applyEffects);
        this.originalPosition = center;
        this.bounds = new AABB(center, center);
    }

    @Override
    protected double getExplosionOffset() {
        return this.mechanicsTarget.before(MechanicVersion.v1_10)
            ? (double) 0.49f
            : super.getExplosionOffset();
    }

    private void mergeEntitiesBeforeExploding() {
        final IteratorSafeOrderedReferenceSet<Entity> entities = this.level().entityTickList.entities;
        int index = entities.indexOf(this.cause);

        entities.createRawIterator();
        // iterate over the entityTickList to find entities that are exploding in the same position.
        while ((index = entities.advanceRawIterator(index)) != Integer.MAX_VALUE) {
            final Entity foundEntity = entities.rawGet(index);

            // Make sure the found entity is alive and it's a mergeable entity.
            if (!(foundEntity instanceof MergeableEntity mergeEntity) || foundEntity.isRemoved()) {
                break;
            }

            // Check if the found entity is the same type and has the same state as the explosion source.
            if (!foundEntity.compareState(this.cause) || !mergeEntity.isSafeToMergeInto(this.cause, true)) {
                break;
            }

            // Merge the found entity into the explosion source
            this.level().mergeHandler.mergeEntity(mergeEntity, this.cause);
        }
        entities.finishRawIterator();
    }

    private int mergeAndGetExplosionPotential() {
        // Try to merge entities before exploding
        if (this.cause.getMergeEntityData().mergeLevel == MergeLevel.NONE) {
            this.mergeEntitiesBeforeExploding();
        }
        return this.cause.getMergeEntityData().count;
    }

    @Override
    protected void beginExplosion() {
        for (int remaining = this.mergeAndGetExplosionPotential() - 1; remaining >= 0; --remaining) {
            final boolean lastCycle = remaining == 0;
            final List<BlockPos> toBlow = this.midExplosion(lastCycle); // search for blocks and impact entities
            final boolean destroyedBlocks = this.finalizeExplosionAndParticles(toBlow); // call events, break blocks and send particles

            if (!lastCycle) {
                final EntityState entityState = this.nextSourceVelocity();
                this.postExplosion(toBlow, destroyedBlocks);
                this.updateExplosionPosition(entityState, destroyedBlocks);
            }
        }
    }

    private List<BlockPos> midExplosion(final boolean lastCycle) {
        final List<BlockPos> explodedPositions = this.swinging < FOUND_ALL_BLOCKS
            ? this.calculateExplodedPositions()
            : List.of();

        final Vec3 center = this.center;
        this.bounds = this.bounds.expand(center);
        this.explosions.add(center);

        if (lastCycle || this.requiresImpactEntities(explodedPositions, center)) {
            this.locateAndImpactEntitiesInBounds(this.bounds, this.explosions);
            this.bounds = new AABB(center, center);
            this.explosions.clear();
        }

        return explodedPositions;
    }

    @Override
    protected void postExplosion(final List<BlockPos> foundBlocks, final boolean destroyedBlocks) {
        super.postExplosion(foundBlocks, destroyedBlocks);
        if (this.swinging >= ALL_DIRECTIONS) {
            // Increment "swinging" if no blocks have been found, and it has swung in every direction.
            // This is used to skip expensive exploded block calculations.
            if (!destroyedBlocks && this.level().sakuraConfig().cannons.explosion.avoidRedundantBlockSearches) {
                this.swinging++;
            } else {
                this.swinging = ALL_DIRECTIONS;
            }
        }
    }

    private void updateSwingingState(final Vec3 momentum, final Vec3 previousMomentum) {
        for (final Direction.Axis axis : Direction.Axis.VALUES) {
            final double current  = momentum.get(axis);
            final double previous = previousMomentum.get(axis);
            if (current == previous || current * previous <= 0.0) {
                this.swinging |= 1 << axis.ordinal();
            }
        }
    }

    private Vec3 getCauseOrigin() {
        return this.cause.origin == null ? this.center : this.cause.origin;
    }

    private EntityState nextSourceVelocity() {
        final Vec3 origin = this.getCauseOrigin(); // valid position to use while creating a temporary entity
        final PrimedTnt tnt = new PrimedTnt(this.level(), origin.x(), origin.y(), origin.z(), null);
        this.cause.entityState().apply(tnt);
        this.impactCannonEntity(tnt, this.center, 1, this.radius() * 2.0f);
        return EntityState.of(tnt);
    }

    private void updateExplosionPosition(final EntityState entityState, final boolean destroyedBlocks) {
        // Before setting entity state, otherwise we might cause issues.
        final Vec3 entityMomentum = this.cause.entityState().momentum();
        final boolean hasMoved;
        if (this.moved) {
            hasMoved = true;
        } else if (this.center.equals(this.cause.position())) {
            hasMoved = false;
        } else {
            final double newMomentumSqr = entityState.momentum().lengthSqr();
            final double oldMomentumSqr = entityMomentum.lengthSqr();
            final double maxExplosionRadiusSqr = Math.pow(this.radius() * 2.0 + 1.0, 2.0);
            hasMoved = oldMomentumSqr <= maxExplosionRadiusSqr || newMomentumSqr <= oldMomentumSqr;
        }

        // Keep track of entity state
        entityState.apply(this.cause);
        this.cause.storeEntityState();

        // Ticking is always required after destroying a block.
        if (destroyedBlocks || hasMoved) {
            this.cause.setFuse(100);
            this.cause.tick();
            this.recalculateExplosionPosition();
            this.moved |= !this.center.equals(this.originalPosition);
        }

        // Update swinging state
        if (this.moved) {
            this.updateSwingingState(entityState.momentum(), entityMomentum);
        } else {
            this.swinging = ALL_DIRECTIONS;
        }
    }
}
