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
    public @Nullable Entity mergeEntity(final Entity entity, final Entity previous, final TrackedMergeHistory mergeHistory) {
        return entity.compareState(previous) ? previous : null;
    }
}
