package me.samsuik.sakura.entity.merge;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.samsuik.sakura.configuration.WorldConfiguration.Cannons.Mechanics.TNTSpread;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class TrackedMergeHistory {
    private final Long2ObjectMap<PositionHistory> historyMap = new Long2ObjectOpenHashMap<>();

    public boolean hasPreviouslyMergedAndMeetsCondition(Entity entity, Entity into, MergeCondition condition) {
        return this.hasPreviouslyMerged(entity, into) && this.hasMetCondition(entity, condition);
    }

    public boolean hasPreviouslyMerged(Entity entity, Entity into) {
        PositionHistory positions = this.getHistory(into, false);
        return positions != null && positions.hasPosition(entity);
    }

    public boolean hasMetCondition(Entity entity, MergeCondition condition) {
        PositionHistory positions = this.getHistory(entity, false);
        return positions != null && positions.hasMetConditions(entity, condition);
    }

    private boolean shouldTrackAllPositions(Entity entity, MergeEntityData mergeEntityData) {
        return entity instanceof FallingBlockEntity
            || mergeEntityData.mergeLevel == MergeLevel.LENIENT
            || entity.level().sakuraConfig().cannons.mechanics.tntSpread == TNTSpread.ALL;
    }

    public void trackHistory(Entity entity, MergeEntityData mergeEntityData) {
        PositionHistory positions = this.getHistory(entity, true);
        LongOpenHashSet originPositions = mergeEntityData.getOriginPositions();
        long gameTime = entity.level().getGameTime();
        boolean retainHistory = positions.hasTicksPassed(gameTime, 160);
        if (!retainHistory && this.shouldTrackAllPositions(entity, mergeEntityData)) {
            originPositions.forEach(pos -> this.historyMap.put(pos, positions));
        }
        positions.trackPositions(originPositions, retainHistory);
    }

    public void expire(long gameTime) {
        this.historyMap.values().removeIf(p -> p.expiry().isExpired(gameTime));
    }

    @Nullable
    @Contract("_, false -> _; _, true -> !null")
    private PositionHistory getHistory(Entity entity, boolean create) {
        long originPosition = entity.getPackedOriginPosition();
        PositionHistory history = this.historyMap.get(originPosition);
        //noinspection ConstantValue
        if (create && history == null) {
            history = new PositionHistory(entity.level().getGameTime());
            this.historyMap.put(originPosition, history);
        }
        return history;
    }

    private static final class PositionHistory {
        private final LongOpenHashSet positions = new LongOpenHashSet();
        private final TickExpiry expiry;
        private final long created;
        private int cycles = 0;

        public PositionHistory(long gameTime) {
            this.expiry = new TickExpiry(gameTime, 200);
            this.created = gameTime;
        }

        public TickExpiry expiry() {
            return this.expiry;
        }

        public boolean hasPosition(Entity entity) {
            this.expiry.refresh(entity.level().getGameTime());
            return this.positions.contains(entity.getPackedOriginPosition());
        }

        public void trackPositions(LongOpenHashSet positions, boolean retain) {
            if (retain) {
                this.positions.retainAll(positions);
            } else {
                this.positions.addAll(positions);
            }
            this.cycles++;
        }

        public boolean hasMetConditions(@NotNull Entity entity, @NotNull MergeCondition condition) {
            long gameTime = entity.level().getGameTime();
            return condition.accept(entity, this.cycles, this.timeSinceCreation(gameTime));
        }

        public boolean hasTicksPassed(long gameTime, int ticks) {
            return this.timeSinceCreation(gameTime) > ticks;
        }

        private long timeSinceCreation(long gameTime) {
            return gameTime - this.created;
        }
    }
}
