package me.samsuik.sakura.entity.merge.strategy;

import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.entity.merge.TrackedMergeHistory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface MergeStrategy {
    /**
     * If this merge strategy requires the merge history to be tracked.
     *
     * @return should track history
     */
    boolean trackHistory();

    /**
     * Tries to merge the first entity into another entity.
     * <p>
     * The first entity should always be positioned right after the second entity in the
     * {@link EntityTickList}. This method should only
     * be called before the first entity and after the second entity has ticked.
     *
     * @param entity current entity
     * @param previous last entity to tick
     * @return success
     */
    @Nullable
    Entity mergeEntity(Entity entity, Entity previous, TrackedMergeHistory mergeHistory);

    /**
     * Gets the {@link MergeStrategy} for the {@link MergeLevel}.
     *
     * @param level provided level
     * @return strategy
     */
    static MergeStrategy from(MergeLevel level) {
        return switch (level) {
            case NONE -> NoneStrategy.INSTANCE;
            case STRICT -> StrictStrategy.INSTANCE;
            case LENIENT -> LenientStrategy.INSTANCE;
            case SPAWN -> SpawnStrategy.INSTANCE;
        };
    }
}
