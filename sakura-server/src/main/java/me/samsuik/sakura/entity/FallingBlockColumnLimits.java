package me.samsuik.sakura.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

/**
 * Falling blocks are able to fall instantly when they are scheduled in order.
 */
public final class FallingBlockColumnLimits {
    private final Long2ObjectOpenHashMap<Column> columns = new Long2ObjectOpenHashMap<>();

    public int count(final BlockPos pos) {
        final long columnKey = ChunkPos.pack(pos.getX(), pos.getZ());
        final Column column = this.columns.computeIfAbsent(columnKey, c -> new Column());
        return column.count(pos);
    }

    public void clear() {
        this.columns.clear();
    }

    private static final class Column {
        private BlockPos lastToFall;
        private int count;

        public int count(final BlockPos pos) {
            if (this.lastToFall == null || pos.getY() == this.lastToFall.getY() + 1) {
                this.count++;
            } else {
                this.count = 0;
            }

            this.lastToFall = pos;
            return this.count;
        }
    }
}
