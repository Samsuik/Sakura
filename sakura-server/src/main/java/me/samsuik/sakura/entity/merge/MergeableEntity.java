package me.samsuik.sakura.entity.merge;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface MergeableEntity {
    MergeEntityData getMergeEntityData();

    boolean isSafeToMergeInto(MergeableEntity entity, boolean ticksLived);

    default boolean tryToRespawnEntity() {
        MergeEntityData mergeData = this.getMergeEntityData();
        int originalCount = mergeData.count;
        if (originalCount > 1) {
            mergeData.count = 0;
            this.respawnEntity(originalCount);
            return true;
        }
        return false;
    }

    void respawnEntity(int count);
}
