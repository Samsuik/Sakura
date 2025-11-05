package me.samsuik.sakura.entity.merge.strategy;

import me.samsuik.sakura.entity.merge.TrackedMergeHistory;
import me.samsuik.sakura.utils.collections.BlockPosToEntityTable;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class LenientStrategy implements MergeStrategy {
    static final LenientStrategy INSTANCE = new LenientStrategy();
    private final BlockPosToEntityTable entityTable = new BlockPosToEntityTable(128);

    @Override
    public boolean trackHistory() {
        return true;
    }

    @Override
    public @Nullable Entity mergeEntity(final Entity entity, final Entity previous, final TrackedMergeHistory mergeHistory) {
        if (entity.compareState(previous)) {
            return previous;
        }

        if (!mergeHistory.hasPreviouslyMerged(entity, previous)) {
            this.entityTable.clear();
        }

        final Entity nextEntity = this.entityTable.put(entity);
        if (nextEntity == null || entity == nextEntity || !nextEntity.level().equals(entity.level())) {
            return null;
        }

        return entity.compareState(nextEntity) ? nextEntity : null;
    }
}
