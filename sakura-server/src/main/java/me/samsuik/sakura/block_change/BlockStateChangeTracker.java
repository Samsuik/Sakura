package me.samsuik.sakura.block_change;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import me.samsuik.sakura.block_change.callback.BlockChangeCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.LongConsumer;

@SuppressWarnings("ConstantValue")
@NullMarked
public final class BlockStateChangeTracker {
    private final Long2ObjectMap<ListeningToPositions> identifiersInUse = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<LongOpenHashSet> chunkAssociatedIdentifiers = new Long2ObjectOpenHashMap<>();
    private final Level level;
    private long identifier = Long.MIN_VALUE;

    public BlockStateChangeTracker(final Level level) {
        this.level = level;
    }

    public long firstChange(final Set<BlockPos> positions, final BlockFilter filter, final Runnable callback) {
        final LongConsumer removeAfterUse = identifier -> {
            callback.run();
            this.stopListening(identifier);
        };

        return this.listenForChanges(positions, filter, BlockChangeCallback.identifier(removeAfterUse));
    }

    public long allChanges(final Set<BlockPos> positions, final BlockFilter filter, final LongConsumer callback) {
        return this.listenForChanges(positions, filter, BlockChangeCallback.identifier(callback));
    }

    public long listenForChanges(final Set<BlockPos> positions, final BlockFilter filter, final BlockChangeCallback callback) {
        final ObjectImmutableList<BlockPos> immutablePositions = new ObjectImmutableList<>(positions);
        final long identifier = this.identifier++;
        final ListeningToPositions listener = new ListeningToPositions(immutablePositions, identifier, filter, callback);

        for (final BlockPos pos : immutablePositions) {
            final long chunkKey = ChunkPos.asLong(pos);
            final LongOpenHashSet chunkIdentifiers = this.chunkAssociatedIdentifiers.computeIfAbsent(chunkKey, k -> new LongOpenHashSet());

            if (!chunkIdentifiers.add(identifier)) {
                continue;
            }

            final LevelChunk chunk = this.level.getChunkIfLoaded(pos);
            if (chunk != null) {
                chunk.getBlockStateWatcher().startListening(listener);
            }
        }

        this.identifiersInUse.put(identifier, listener);
        return identifier;
    }

    public void stopListening(final long identifier) {
        final ListeningToPositions listener = this.identifiersInUse.remove(identifier);
        if (listener == null) {
            return;
        }

        for (final BlockPos pos : listener.positions()) {
            final long chunkKey = ChunkPos.asLong(pos);
            final LongOpenHashSet chunkIdentifiers = this.chunkAssociatedIdentifiers.get(chunkKey);

            if (chunkIdentifiers == null || !chunkIdentifiers.remove(identifier)) {
                continue;
            }

            final LevelChunk chunk = this.level.getChunkIfLoaded(pos);
            if (chunk != null) {
                chunk.getBlockStateWatcher().stopListening(listener);
            }
        }
    }

    public List<ListeningToPositions> getListenersAtChunk(final LevelChunk chunk) {
        final LongOpenHashSet identifiers = this.chunkAssociatedIdentifiers.get(chunk.coordinateKey);
        if (identifiers == null) {
            return Collections.emptyList();
        }

        final List<ListeningToPositions> listeners = new ArrayList<>();
        for (final long identifier : identifiers) {
            listeners.add(this.identifiersInUse.get(identifier));
        }

        return listeners;
    }
}
