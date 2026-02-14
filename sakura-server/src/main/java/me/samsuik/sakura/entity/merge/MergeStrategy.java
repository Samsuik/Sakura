package me.samsuik.sakura.entity.merge;

import me.samsuik.sakura.utils.collections.FixedSizeCustomObjectTable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.jetbrains.annotations.NotNull;

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
    Entity mergeEntity(@NotNull Entity entity, @NotNull Entity previous, @NotNull TrackedMergeHistory mergeHistory);

    /**
     * Gets the {@link MergeStrategy} for the {@link MergeLevel}.
     *
     * @param level provided level
     * @return strategy
     */
    static MergeStrategy from(MergeLevel level) {
        return switch (level) {
            case NONE -> None.INSTANCE;
            case STRICT -> Strict.INSTANCE;
            case LENIENT -> Lenient.INSTANCE;
            case SPAWN -> Spawn.INSTANCE;
        };
    }

    final class None implements MergeStrategy {
        private static final None INSTANCE = new None();

        @Override
        public boolean trackHistory() {
            return false;
        }

        @Override
        public Entity mergeEntity(@NotNull Entity entity, @NotNull Entity previous, @NotNull TrackedMergeHistory mergeHistory) {
            return null;
        }
    }

    final class Strict implements MergeStrategy {
        private static final Strict INSTANCE = new Strict();

        @Override
        public boolean trackHistory() {
            return false;
        }

        @Override
        public Entity mergeEntity(@NotNull Entity entity, @NotNull Entity previous, @NotNull TrackedMergeHistory mergeHistory) {
            // return entity.compareState(previous) ? previous : null;
            return null;
        }
    }

    final class Lenient implements MergeStrategy {
        private static final Lenient INSTANCE = new Lenient();
        private final FixedSizeCustomObjectTable<Entity> entityTable = new FixedSizeCustomObjectTable<>(512, entity -> {
            return entity.blockPosition().hashCode();
        });

        @Override
        public boolean trackHistory() {
            return true;
        }

        @Override
        public Entity mergeEntity(@NotNull Entity entity, @NotNull Entity previous, @NotNull TrackedMergeHistory mergeHistory) {
            // if (entity.compareState(previous)) {
            //     return previous;
            // }

            Entity nextEntity = this.entityTable.getAndWrite(entity);
            if (nextEntity == null || entity == nextEntity || !nextEntity.level().equals(entity.level())) {
                return null;
            }

            // return mergeHistory.hasPreviousMerged(entity, nextEntity) && entity.compareState(nextEntity) ? nextEntity : null;
            return mergeHistory.hasPreviousMerged(entity, nextEntity) ? nextEntity : null;
        }
    }

    final class Spawn implements MergeStrategy {
        private static final Spawn INSTANCE = new Spawn();
        private static final MergeCondition CONDITION = (e, shots, time) -> (shots > 16 || time >= 200);

        @Override
        public boolean trackHistory() {
            return true;
        }

        @Override
        public Entity mergeEntity(@NotNull Entity entity, @NotNull Entity previous, @NotNull TrackedMergeHistory mergeHistory) {
            final Entity mergeInto;
            if (entity.tickCount == 1 && mergeHistory.hasPreviousMerged(entity, previous) && mergeHistory.hasMetCondition(previous, CONDITION)) {
                mergeInto = previous;
            } else {
                // mergeInto = entity.compareState(previous) ? previous : null;
                mergeInto = null;
            }
            return mergeInto;
        }
    }
}
