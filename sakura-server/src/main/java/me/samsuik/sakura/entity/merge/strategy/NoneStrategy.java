package me.samsuik.sakura.entity.merge.strategy;

import me.samsuik.sakura.entity.merge.TrackedMergeHistory;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class NoneStrategy implements MergeStrategy {
    static final NoneStrategy INSTANCE = new NoneStrategy();

    @Override
    public boolean trackHistory() {
        return false;
    }

    @Override
    @Nullable
    public Entity mergeEntity(Entity entity, Entity previous, TrackedMergeHistory mergeHistory) {
        return null;
    }
}
