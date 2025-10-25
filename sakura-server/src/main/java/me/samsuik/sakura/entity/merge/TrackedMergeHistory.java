package me.samsuik.sakura.entity.merge;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.samsuik.sakura.configuration.WorldConfiguration.Cannons.Mechanics.TNTSpread;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class TrackedMergeHistory {
    private final Long2ObjectMap<PositionHistory> historyMap = new Long2ObjectOpenHashMap<>();

    public boolean hasPreviouslyMergedAndMeetsCondition(final Entity entity, final Entity into, final MergeCondition condition) {
        return this.hasPreviouslyMerged(entity, into) && this.hasMetCondition(entity, condition);
    }

    public boolean hasPreviouslyMerged(final Entity entity, final Entity into) {
        final PositionHistory positions = this.getHistory(into, false);
        return positions != null && positions.hasPosition(entity);
    }

    public boolean hasMetCondition(final Entity entity, final MergeCondition condition) {
        final PositionHistory positions = this.getHistory(entity, false);
        return positions != null && positions.hasMetConditions(entity, condition);
    }

    private boolean shouldTrackAllPositions(final Entity entity, final MergeEntityData mergeEntityData) {
        return entity instanceof FallingBlockEntity
            || mergeEntityData.mergeLevel == MergeLevel.LENIENT
            || entity.level().sakuraConfig().cannons.mechanics.tntSpread == TNTSpread.ALL;
    }

    public void trackHistory(final Entity entity, final MergeEntityData mergeEntityData) {
        final PositionHistory positionHistory = this.getHistory(entity, true);
        final LongOpenHashSet positions = mergeEntityData.getOriginPositions();
        final long gameTime = entity.level().getGameTime();
        final boolean retainHistory = positionHistory.hasTicksPassed(gameTime, 160);

        if (!retainHistory && this.shouldTrackAllPositions(entity, mergeEntityData)) {
            positions.forEach(pos -> this.historyMap.put(pos, positionHistory));
        }

        positionHistory.trackPositions(positions, retainHistory);
    }

    public void expire(final long gameTime) {
        this.historyMap.values().removeIf(history -> history.expiry().isExpired(gameTime));
    }

    @Contract("_, false -> _; _, true -> !null")
    private @Nullable PositionHistory getHistory(final Entity entity, final boolean create) {
        final long position = entity.getPackedOriginPosition();
        PositionHistory history = this.historyMap.get(position);
        //noinspection ConstantValue
        if (create && history == null) {
            history = new PositionHistory(entity.level().getGameTime());
            this.historyMap.put(position, history);
        }
        return history;
    }

    private static final class PositionHistory {
        private final LongOpenHashSet positions = new LongOpenHashSet();
        private final TickExpiry expiry;
        private final long created;
        private int cycles = 0;

        public PositionHistory(final long gameTime) {
            this.expiry = new TickExpiry(gameTime, 200);
            this.created = gameTime;
        }

        public TickExpiry expiry() {
            return this.expiry;
        }

        public boolean hasPosition(final Entity entity) {
            this.expiry.refresh(entity.level().getGameTime());
            return this.positions.contains(entity.getPackedOriginPosition());
        }

        public void trackPositions(final LongOpenHashSet positions, final boolean retain) {
            if (retain) {
                this.positions.retainAll(positions);
            } else {
                this.positions.addAll(positions);
            }
            this.cycles++;
        }

        public boolean hasMetConditions(final Entity entity, final MergeCondition condition) {
            final long gameTime = entity.level().getGameTime();
            return condition.accept(entity, this.cycles, this.timeSinceCreation(gameTime));
        }

        public boolean hasTicksPassed(final long gameTime, final int ticks) {
            return this.timeSinceCreation(gameTime) > ticks;
        }

        private long timeSinceCreation(final long gameTime) {
            return gameTime - this.created;
        }
    }
}
