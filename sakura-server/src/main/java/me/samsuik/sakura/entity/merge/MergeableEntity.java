package me.samsuik.sakura.entity.merge;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface MergeableEntity {
    MergeEntityData getMergeEntityData();

    boolean isSafeToMergeInto(final MergeableEntity entity, final boolean ticksLived);

    default boolean tryToRespawnEntity() {
        final MergeEntityData mergeData = this.getMergeEntityData();
        final int originalCount = mergeData.count;
        if (originalCount > 1) {
            mergeData.count = 0;
            this.respawnEntity(originalCount);
            return true;
        }
        return false;
    }

    void respawnEntity(final int count);
}
