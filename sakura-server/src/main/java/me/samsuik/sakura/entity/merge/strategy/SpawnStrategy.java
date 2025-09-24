package me.samsuik.sakura.entity.merge.strategy;

import me.samsuik.sakura.entity.merge.MergeCondition;
import me.samsuik.sakura.entity.merge.TrackedMergeHistory;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class SpawnStrategy implements MergeStrategy {
    static final SpawnStrategy INSTANCE = new SpawnStrategy();
    private static final MergeCondition CONDITION = (e, shots, time) -> (shots > 16 || time >= 200);

    @Override
    public boolean trackHistory() {
        return true;
    }

    @Override
    public @Nullable Entity mergeEntity(final Entity entity, final Entity previous, final TrackedMergeHistory mergeHistory) {
        final Entity mergeInto;
        if (entity.tickCount == 1 && mergeHistory.hasPreviouslyMergedAndMeetsCondition(entity, previous, CONDITION)) {
            mergeInto = previous;
        } else {
            mergeInto = entity.compareState(previous) ? previous : null;
        }
        return mergeInto;
    }
}
