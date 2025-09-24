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

    public BlockChangeTracker(final Level level) {
        this.level = level;
    }

    public long listenForChangesOnce(final BlockChangeFilter filter, final Set<BlockPos> positions, final Runnable callback) {
        final LongConsumer singleUseCallback = (identifier) -> {
            callback.run();
            this.stopListening(identifier);
        };
        return this.listenForChanges(filter, positions, singleUseCallback);
    }

    public long listenForChanges(final BlockChangeFilter filter, final Set<BlockPos> positions, final LongConsumer callback) {
        final long identifier = this.identifier++;
        final Listener listener = new Listener(filter, positions, identifier, callback);
        for (final ChunkPos chunkPos : getChunkPositions(positions)) {
            this.addListenerToChunk(chunkPos, listener);
        }
        this.identifiersInUse.put(identifier, listener);
        return identifier;
    }

    public void stopListening(final long identifier) {
        final Listener listener = this.identifiersInUse.remove(identifier);
        //noinspection ConstantValue
        if (listener != null) {
            for (final ChunkPos chunkPos : getChunkPositions(listener.positions())) {
                this.removeListenerFronChunk(chunkPos, listener);
            }
        }
    }

    private void removeListenerFronChunk(final ChunkPos chunkPos, final Listener listener) {
        final long chunkKey = chunkPos.toLong();
        final List<Listener> listeners = this.chunkListeners.computeIfPresent(chunkKey, (k, present) -> {
            present.remove(listener);
            return present.isEmpty() ? null : present;
        });
        this.updateListeners(chunkPos, Objects.requireNonNullElse(listeners, Collections.emptyList()));
    }

    private void addListenerToChunk(final ChunkPos chunkPos, final Listener listener) {
        final long chunkKey = chunkPos.toLong();
        final List<Listener> listeners = this.chunkListeners.computeIfAbsent(chunkKey, i -> new ArrayList<>());
        listeners.add(listener);
        this.updateListeners(chunkPos, listeners);
    }

    private void updateListeners(final ChunkPos chunkPos, final List<Listener> listeners) {
        final LevelChunk chunk = ((ServerLevel) this.level).chunkSource.getChunkAtIfLoadedImmediately(chunkPos.x, chunkPos.z);
        if (chunk != null) {
            chunk.updateBlockChangeListeners(List.copyOf(listeners));
        }
    }

    public List<Listener> getListenersForChunk(final ChunkPos chunkPos) {
        return List.copyOf(this.chunkListeners.getOrDefault(chunkPos.toLong(), Collections.emptyList()));
    }

    private static Set<ChunkPos> getChunkPositions(final Set<BlockPos> positions) {
        final Set<ChunkPos> chunkPositions = new ObjectOpenHashSet<>();
        for (final BlockPos pos : positions) {
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

        boolean test(final Level level, final BlockPos pos, final BlockState newBlock, final BlockState oldBlock);
    }

    public record Listener(BlockChangeFilter filter, Set<BlockPos> positions, long identifier, LongConsumer callback) {
        public void call() {
            this.callback.accept(this.identifier);
        }

        public boolean test(final Level level, final BlockPos pos, final BlockState newBlock, final BlockState oldBlock) {
            return this.filter.test(level, pos, newBlock, oldBlock)
                && this.positions.contains(pos);
        }
    }
}
