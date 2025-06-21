package me.samsuik.sakura.explosion.special;

import ca.spottedleaf.moonrise.common.list.IteratorSafeOrderedReferenceSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.samsuik.sakura.entity.EntityState;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.entity.merge.MergeableEntity;
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

import java.util.List;
import java.util.function.Consumer;

public final class TntExplosion extends SpecialisedExplosion<PrimedTnt> {
    private static final int ALL_DIRECTIONS = 0b111;
    private static final int FOUND_ALL_BLOCKS = ALL_DIRECTIONS + 12;

    private final Vec3 originalPosition;
    private final List<Vec3> explosions = new ObjectArrayList<>();
    private AABB bounds;
    private int swinging = 0;
    private boolean moved = false;

    public TntExplosion(ServerLevel level, PrimedTnt tnt, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, Vec3 center, float power, boolean createFire, BlockInteraction destructionType, Consumer<SpecialisedExplosion<PrimedTnt>> applyEffects) {
        super(level, tnt, damageSource, behavior, center, power, createFire, destructionType, applyEffects);
        this.originalPosition = center;
        this.bounds = new AABB(center, center);
    }

    // Sakura start - physics version api
    @Override
    protected double getExplosionOffset() {
        return this.physics.before(1_10_0) ? (double) 0.49f : super.getExplosionOffset();
    }
    // Sakura end - physics version api

    @Override
    protected int getExplosionCount() {
        if (this.cause.getMergeEntityData().mergeLevel == MergeLevel.NONE) {
            this.mergeEntitiesBeforeExploding();
        }
        return this.cause.getMergeEntityData().count;
    }

    @Override
    protected void startExplosion() {
        for (int i = this.getExplosionCount() - 1; i >= 0; --i) {
            boolean lastCycle = i == 0;
            List<BlockPos> toBlow = this.midExplosion(lastCycle); // search for blocks and impact entities
            boolean destroyedBlocks = this.finalizeExplosionAndParticles(toBlow); // call events, break blocks and send particles

            if (!lastCycle) {
                EntityState entityState = this.nextSourceVelocity();
                this.postExplosion(toBlow, destroyedBlocks);
                this.updateExplosionPosition(entityState, destroyedBlocks);
            }
        }
    }

    private List<BlockPos> midExplosion(boolean lastCycle) {
        final List<BlockPos> explodedPositions;
        if (this.swinging < FOUND_ALL_BLOCKS) {
            explodedPositions = this.calculateExplodedPositions();
        } else {
            explodedPositions = List.of();
        }

        Vec3 center = this.center;
        this.bounds = this.bounds.expand(center);
        this.explosions.add(center);

        if (lastCycle || this.requiresImpactEntities(explodedPositions, center)) {
            this.locateAndImpactEntitiesInBounds();
            this.bounds = new AABB(center, center);
            this.explosions.clear();
        }

        return explodedPositions;
    }

    @Override
    protected void postExplosion(List<BlockPos> foundBlocks, boolean destroyedBlocks) {
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

    private void updateSwingingState(Vec3 momentum, Vec3 previousMomentum) {
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            double current  = momentum.get(axis);
            double previous = previousMomentum.get(axis);
            if (current == previous || current * previous <= 0.0) {
                this.swinging |= 1 << axis.ordinal();
            }
        }
    }

    private Vec3 getCauseOrigin() {
        return this.cause.origin == null ? this.center : this.cause.origin;
    }

    private EntityState nextSourceVelocity() {
        Vec3 origin = this.getCauseOrigin(); // valid position to use while creating a temporary entity
        PrimedTnt tnt = new PrimedTnt(this.level(), origin.x(), origin.y(), origin.z(), null);
        this.cause.entityState().apply(tnt);
        this.impactCannonEntity(tnt, this.center, 1, this.radius() * 2.0f);
        return EntityState.of(tnt);
    }

    private void updateExplosionPosition(EntityState entityState, boolean destroyedBlocks) {
        // Before setting entity state, otherwise we might cause issues.
        Vec3 entityMomentum = this.cause.entityState().momentum();
        final boolean hasMoved;
        if (this.moved) {
            hasMoved = true;
        } else if (this.center.equals(this.cause.position())) {
            hasMoved = false;
        } else {
            double newMomentum = entityState.momentum().lengthSqr();
            double oldMomentum = entityMomentum.lengthSqr();
            hasMoved = oldMomentum <= Math.pow(this.radius() * 2.0 + 1.0, 2.0) || newMomentum <= oldMomentum;
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

    private void mergeEntitiesBeforeExploding() {
        IteratorSafeOrderedReferenceSet<Entity> entities = this.level().entityTickList.entities;
        int index = entities.indexOf(this.cause);

        entities.createRawIterator();
        // iterate over the entityTickList to find entities that are exploding in the same position.
        while ((index = entities.advanceRawIterator(index)) != -1) {
            Entity foundEntity = entities.getListRaw()[index];
            if (!(foundEntity instanceof MergeableEntity mergeEntity) || foundEntity.isRemoved() || !foundEntity.compareState(this.cause) || !mergeEntity.isSafeToMergeInto(this.cause, true))
                break;
            this.level().mergeHandler.mergeEntity(mergeEntity, this.cause);
        }
        entities.finishRawIterator();
    }

    private void locateAndImpactEntitiesInBounds() {
        double radius = this.radius() * 2.0f;
        AABB bb = this.bounds;

        Vec3 center = bb.getCenter();
        double change = Math.max(bb.getXsize(), Math.max(bb.getYsize(), bb.getZsize()));
        double maxDistanceSqr = Math.pow(radius + change, 2.0);
        boolean positionChanged = change != 0.0;

        this.forEachEntitySliceInBounds(bb.inflate(radius), entities -> {
            if (positionChanged) {
                this.impactEntitiesSwinging(entities, center, radius, maxDistanceSqr);
            } else {
                this.impactEntitiesFromPosition(entities, this.explosions.getFirst(), this.explosions.size(), radius);
            }
        });
    }

    private void impactEntitiesSwinging(Entity[] entities, Vec3 center, double radius, double maxDistanceSqr) {
        for (int i = 0; i < entities.length; ++i) {
            Entity entity = entities[i];
            if (entity == null) break;

            if (entity != this.source && !entity.ignoreExplosion(this) && entity.distanceToSqr(center.x, center.y, center.z) <= maxDistanceSqr) {
                this.impactEntitySwinging(entity, radius);
            }

            if (entities[i] != entity) {
                i--;
            }
        }
    }

    private void impactEntitySwinging(Entity entity, double radius) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < this.explosions.size(); i++) {
            this.impactEntity(entity, this.explosions.get(i), 1, radius);
        }
    }
}
