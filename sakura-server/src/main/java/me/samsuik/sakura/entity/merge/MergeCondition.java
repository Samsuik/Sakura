package me.samsuik.sakura.entity.merge;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface MergeCondition {
    default MergeCondition and(final MergeCondition condition) {
        return (e,c,t) -> this.accept(e,c,t) && condition.accept(e,c,t);
    }

    default MergeCondition or(final MergeCondition condition) {
        return (e,c,t) -> this.accept(e,c,t) || condition.accept(e,c,t);
    }

    boolean accept(final Entity entity, final int attempts, final long sinceCreation);
}
