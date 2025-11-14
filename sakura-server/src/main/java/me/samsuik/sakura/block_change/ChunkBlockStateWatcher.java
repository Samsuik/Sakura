package me.samsuik.sakura.block_change;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@NullMarked
public final class ChunkBlockStateWatcher {
    private final Map<BlockPos, List<ListeningToPositions>> positionListeners = new Object2ObjectOpenHashMap<>();

    public void blockChange(
        final Level level,
        final BlockPos pos,
        final BlockState newBlock,
        final BlockState oldBlock
    ) {
        final List<ListeningToPositions> changes = this.positionListeners.get(pos);
        if (changes != null) {
            for (final ListeningToPositions change : changes) {
                change.onChange(level, pos, newBlock, oldBlock);
            }
        }
    }

    public void load(final LevelChunk chunk) {
        for (final ListeningToPositions listener : chunk.level.blockStateChangeTracker.getListenersAtChunk(chunk)) {
            this.startListening(listener);
        }
    }

    public void startListening(final ListeningToPositions listener) {
        for (final BlockPos pos : listener.positions()) {
            this.positionListeners.computeIfAbsent(pos, p -> new CopyOnWriteArrayList<>()).add(listener);
        }
    }

    public void stopListening(final ListeningToPositions listener) {
        for (final BlockPos pos : listener.positions()) {
            final List<ListeningToPositions> listeners = this.positionListeners.get(pos);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }
}
