package me.samsuik.sakura.entity.merge;

import me.samsuik.sakura.entity.merge.strategy.MergeStrategy;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class EntityMergeHandler {
    private final TrackedMergeHistory trackedHistory = new TrackedMergeHistory();

    /**
     * Tries to merge the provided entities using the {@link MergeStrategy}.
     *
     * @param previous the last entity to tick
     * @param entity the entity being merged
     * @return success
     */
    public boolean tryMerge(@Nullable Entity entity, @Nullable Entity previous) {
        if (entity instanceof MergeableEntity mergeEntity && previous instanceof MergeableEntity) {
            MergeEntityData mergeEntityData = mergeEntity.getMergeEntityData();
            MergeStrategy strategy = MergeStrategy.from(mergeEntityData.mergeLevel);
            Entity into = strategy.mergeEntity(entity, previous, this.trackedHistory);
            if (into instanceof MergeableEntity intoEntity && !into.isRemoved() && mergeEntity.isSafeToMergeInto(intoEntity, strategy.trackHistory())) {
                return this.mergeEntity(mergeEntity, intoEntity);
            }
        }

        return false;
    }

    /**
     * Stores the merged data of the provided entities if the {@link MergeStrategy} requires it.
     *
     * @param entity provided entity
     */
    public void removeEntity(@Nullable Entity entity) {
        if (entity instanceof MergeableEntity mergeEntity) {
            MergeEntityData mergeEntityData = mergeEntity.getMergeEntityData();
            MergeStrategy strategy = MergeStrategy.from(mergeEntityData.mergeLevel);
            if (mergeEntityData.hasMerged() && strategy.trackHistory()) {
                this.trackedHistory.trackHistory(entity, mergeEntityData);
            }
        }
    }

    /**
     * Called every 200 ticks and the tick is used remove any unneeded merge history.
     *
     * @param tick server tick
     */
    public void expire(long tick) {
        this.trackedHistory.expire(tick);
    }

    /**
     * Merges the first entity into the second. The entity merge count can be retrieved through the {@link MergeEntityData}.
     * <p>
     * This method also updates the bukkit handle so that plugins reference the first entity after the second entity has been removed.
     *
     * @param mergeEntity the first entity
     * @param into the entity to merge into
     * @return if successful
     */
    public boolean mergeEntity(MergeableEntity mergeEntity, MergeableEntity into) {
        MergeEntityData entities = mergeEntity.getMergeEntityData();
        MergeEntityData mergeInto = into.getMergeEntityData();
        mergeInto.mergeWith(entities); // merge entities together

        // discard the entity and update the bukkit handle
        Entity nmsEntity = (Entity) mergeEntity;
        nmsEntity.discard();
        nmsEntity.updateBukkitHandle((Entity) into);
        return true;
    }
}
