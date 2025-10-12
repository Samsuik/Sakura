package me.samsuik.sakura.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
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
    private final Long2ObjectMap<List<Listener>> chunkSectionListeners = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Listener> identifiersInUse = new Long2ObjectOpenHashMap<>();
    private final Level level;
    private long identifier = Long.MIN_VALUE;

    public BlockChangeTracker(final Level level) {
        this.level = level;
    }

    public long listenForChangesOnce(final BlockChangeFilter filter, final Set<BlockPos> positions, final Runnable callback) {
        final LongConsumer singleUseCallback = identifier -> {
            callback.run();
            this.stopListening(identifier);
        };
        return this.listenForChanges(filter, positions, singleUseCallback);
    }

    public long listenForChanges(final BlockChangeFilter filter, final Set<BlockPos> positions, final LongConsumer callback) {
        final long identifier = this.identifier++;
        final Listener listener = new Listener(filter, positions, identifier, callback);

        for (final long sectionPos : getSectionPositions(positions)) {
            this.addListenerToSection(sectionPos, listener);
        }

        this.identifiersInUse.put(identifier, listener);
        return identifier;
    }

    public void stopListening(final long identifier) {
        final Listener listener = this.identifiersInUse.remove(identifier);
        //noinspection ConstantValue
        if (listener != null) {
            for (final long sectionPos : getSectionPositions(listener.positions())) {
                this.removeListenerFromSection(sectionPos, listener);
            }
        }
    }

    private static LongOpenHashSet getSectionPositions(final Set<BlockPos> positions) {
        final LongOpenHashSet sections = new LongOpenHashSet();
        for (final BlockPos pos : positions) {
            sections.add(SectionPos.asLong(pos));
        }
        return sections;
    }

    private void removeListenerFromSection(final long sectionPos, final Listener listener) {
        final List<Listener> listeners = this.chunkSectionListeners.computeIfPresent(sectionPos, (k, present) -> {
            present.remove(listener);
            return present.isEmpty() ? null : present;
        });
        this.updateListeners(sectionPos, Objects.requireNonNullElse(listeners, Collections.emptyList()));
    }

    private void addListenerToSection(final long sectionPos, final Listener listener) {
        final List<Listener> listeners = this.chunkSectionListeners.computeIfAbsent(sectionPos, k -> new ArrayList<>());
        listeners.add(listener);
        this.updateListeners(sectionPos, listeners);
    }

    private void updateListeners(final long sectionPos, final List<Listener> listeners) {
        final int chunkX = SectionPos.x(sectionPos);
        final int chunkZ = SectionPos.x(sectionPos);
        final LevelChunk chunk = ((ServerLevel) this.level).chunkSource.getChunkAtIfLoadedImmediately(chunkX, chunkZ);

        if (chunk != null) {
            final Int2ObjectMap<List<Listener>> sectionListeners = Int2ObjectMaps.singleton(
                SectionPos.y(sectionPos),
                List.copyOf(listeners)
            );
            chunk.updateBlockChangeListeners(sectionListeners);
        }
    }

    public Int2ObjectMap<List<Listener>> getListenersForChunk(final ChunkPos chunkPos, final int minSection, final int maxSection) {
        final Int2ObjectOpenHashMap<List<Listener>> sectionListeners = new Int2ObjectOpenHashMap<>();
        for (int sectionY = minSection; sectionY <= maxSection; ++sectionY) {
            final long sectionPos = SectionPos.asLong(chunkPos.x, sectionY, chunkPos.z);
            final List<Listener> listeners = this.chunkSectionListeners.getOrDefault(sectionPos, Collections.emptyList());
            sectionListeners.put(sectionY, List.copyOf(listeners));
        }

        return sectionListeners;
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
