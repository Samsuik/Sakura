package me.samsuik.sakura.entity.merge;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface MergeCondition {
    default MergeCondition and(MergeCondition condition) {
        return (e,c,t) -> this.accept(e,c,t) && condition.accept(e,c,t);
    }

    default MergeCondition or(MergeCondition condition) {
        return (e,c,t) -> this.accept(e,c,t) || condition.accept(e,c,t);
    }

    boolean accept(Entity entity, int attempts, long sinceCreation);
}
