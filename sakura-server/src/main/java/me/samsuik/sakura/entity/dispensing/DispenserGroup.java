package me.samsuik.sakura.entity.dispensing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public final class DispenserGroup {
    private final Long2ObjectMap<BlockPos> columns = new Long2ObjectOpenHashMap<>();

    public BlockPos getSpawnPosition(final BlockPos pos) {
        final long column = ChunkPos.asLong(pos.getX(), pos.getZ());
        return this.columns.computeIfAbsent(column, c -> pos);
    }
}
