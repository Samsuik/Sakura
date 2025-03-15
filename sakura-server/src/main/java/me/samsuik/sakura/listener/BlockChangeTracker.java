package me.samsuik.sakura.listener;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.function.LongConsumer;

@NullMarked
public final class BlockChangeTracker {
    private final Long2ObjectMap<List<Listener>> chunkListeners = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Listener> identifiersInUse = new Long2ObjectOpenHashMap<>();
    private final Level level;
    private long identifier = Long.MIN_VALUE;

    public BlockChangeTracker(Level level) {
        this.level = level;
    }

    public long listenForChangesOnce(BlockChangeFilter filter, Set<BlockPos> positions, Runnable callback) {
        LongConsumer singleUseCallback = (identifier) -> {
            callback.run();
            this.stopListening(identifier);
        };
        return this.listenForChanges(filter, positions, singleUseCallback);
    }

    public long listenForChanges(BlockChangeFilter filter, Set<BlockPos> positions, LongConsumer callback) {
        long identifier = this.identifier++;
        Listener listener = new Listener(
            filter, positions, identifier, callback
        );
        for (ChunkPos chunkPos : getChunkPositions(positions)) {
            this.addListenerToChunk(chunkPos, listener);
        }
        this.identifiersInUse.put(identifier, listener);
        return identifier;
    }

    public void stopListening(long identifier) {
        Listener listener = this.identifiersInUse.remove(identifier);
        if (listener != null) {
            for (ChunkPos chunkPos : getChunkPositions(listener.positions())) {
                this.removeListenerFronChunk(chunkPos, listener);
            }
        }
    }

    private void removeListenerFronChunk(ChunkPos chunkPos, Listener listener) {
        long chunkKey = chunkPos.toLong();
        List<Listener> listeners = this.chunkListeners.computeIfPresent(chunkKey, (k, present) -> {
            present.remove(listener);
            return present.isEmpty() ? null : present;
        });
        this.updateListeners(chunkPos, Objects.requireNonNullElse(listeners, Collections.emptyList()));
    }

    private void addListenerToChunk(ChunkPos chunkPos, Listener listener) {
        long chunkKey = chunkPos.toLong();
        List<Listener> listeners = this.chunkListeners.computeIfAbsent(chunkKey, i -> new ArrayList<>());
        listeners.add(listener);
        this.updateListeners(chunkPos, listeners);
    }

    private void updateListeners(ChunkPos chunkPos, List<Listener> listeners) {
        LevelChunk chunk = ((ServerLevel) this.level).chunkSource.getChunkAtIfLoadedImmediately(chunkPos.x, chunkPos.z);
        if (chunk != null) {
            chunk.updateBlockChangeListeners(List.copyOf(listeners));
        }
    }

    public List<Listener> getListenersForChunk(ChunkPos chunkPos) {
        return this.chunkListeners.getOrDefault(chunkPos.toLong(), Collections.emptyList());
    }

    private static Set<ChunkPos> getChunkPositions(Set<BlockPos> positions) {
        Set<ChunkPos> chunkPositions = new ObjectOpenHashSet<>();
        for (BlockPos pos : positions) {
            chunkPositions.add(new ChunkPos(pos));
        }
        return chunkPositions;
    }

    public interface BlockChangeFilter {
        BlockChangeFilter ANY = (l, p, n, o) -> true;

        BlockChangeFilter REDSTONE_COMPONENT = (level, pos, oldBlock, newBlock) -> {
            return newBlock.isRedstoneConductor(level, pos) != oldBlock.isRedstoneConductor(level, pos)
                || newBlock.isSignalSource() != oldBlock.isSignalSource();
        };

        boolean test(Level level, BlockPos pos, BlockState newBlock, BlockState oldBlock);
    }

    public record Listener(BlockChangeFilter filter, Set<BlockPos> positions,
                           long identifier, LongConsumer callback) {
        public void call() {
            this.callback.accept(this.identifier);
        }

        public boolean test(Level level, BlockPos pos, BlockState newBlock, BlockState oldBlock) {
            return this.filter.test(level, pos, newBlock, oldBlock)
                && this.positions.contains(pos);
        }
    }
}
