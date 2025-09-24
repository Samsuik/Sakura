package me.samsuik.sakura.utils.collections;

import ca.spottedleaf.moonrise.common.list.ReferenceList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.level.ChunkMap;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;

@NullMarked
public final class TrackedEntityChunkMap extends Int2ObjectOpenHashMap<ChunkMap.TrackedEntity> {
    private final ReferenceList<ChunkMap.TrackedEntity> trackedEntityList = new ReferenceList<>();

    @Override
    public ChunkMap.TrackedEntity put(final int index, final ChunkMap.TrackedEntity trackedEntity) {
        final ChunkMap.TrackedEntity tracked = super.put(index, trackedEntity);
        //noinspection ConstantValue
        if (tracked != null) {
            this.trackedEntityList.remove(trackedEntity);
        }
        this.trackedEntityList.add(trackedEntity);
        return tracked;
    }

    @Override
    public ChunkMap.TrackedEntity remove(final int index) {
        final ChunkMap.TrackedEntity tracked = super.remove(index);
        this.trackedEntityList.remove(tracked);
        return tracked;
    }

    @Override
    public ObjectCollection<ChunkMap.TrackedEntity> values() {
        return new AbstractObjectCollection<>() {
            @Override
            public ObjectIterator<ChunkMap.TrackedEntity> iterator() {
                return new TrackedEntityIterator();
            }

            @Override
            public int size() {
                return TrackedEntityChunkMap.this.size();
            }
        };
    }

    private final class TrackedEntityIterator implements ObjectIterator<ChunkMap.TrackedEntity> {
        private final Iterator<ChunkMap.TrackedEntity> backingItr = TrackedEntityChunkMap.this.trackedEntityList.iterator();

        @Override
        public boolean hasNext() {
            return this.backingItr.hasNext();
        }

        @Override
        public ChunkMap.TrackedEntity next() {
            return this.backingItr.next();
        }

        @Override
        public void remove() {
            this.backingItr.remove();
        }
    }
}
