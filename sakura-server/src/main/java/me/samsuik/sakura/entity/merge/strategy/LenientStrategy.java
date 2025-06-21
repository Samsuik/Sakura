package me.samsuik.sakura.entity.merge.strategy;

import me.samsuik.sakura.entity.merge.TrackedMergeHistory;
import me.samsuik.sakura.utils.collections.FixedSizeCustomObjectTable;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class LenientStrategy implements MergeStrategy {
    static final LenientStrategy INSTANCE = new LenientStrategy();

    private final FixedSizeCustomObjectTable<Entity> entityTable = new FixedSizeCustomObjectTable<>(512, entity -> {
        return entity.blockPosition().hashCode();
    });

    @Override
    public boolean trackHistory() {
        return true;
    }

    @Override
    @Nullable
    public Entity mergeEntity(Entity entity, Entity previous, TrackedMergeHistory mergeHistory) {
        if (entity.compareState(previous)) {
            return previous;
        }

        if (!mergeHistory.hasPreviouslyMerged(entity, previous)) {
            this.entityTable.clear();
        }

        final Entity nextEntity = this.entityTable.getAndWrite(entity);
        if (nextEntity == null || entity == nextEntity || !nextEntity.level().equals(entity.level())) {
            return null;
        }

        return entity.compareState(nextEntity) ? nextEntity : null;
    }
}
