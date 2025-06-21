package me.samsuik.sakura.entity.merge.strategy;

import me.samsuik.sakura.entity.merge.TrackedMergeHistory;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class StrictStrategy implements MergeStrategy {
    static final StrictStrategy INSTANCE = new StrictStrategy();

    @Override
    public boolean trackHistory() {
        return false;
    }

    @Override
    @Nullable
    public Entity mergeEntity(Entity entity, Entity previous, TrackedMergeHistory mergeHistory) {
        return entity.compareState(previous) ? previous : null;
    }
}
