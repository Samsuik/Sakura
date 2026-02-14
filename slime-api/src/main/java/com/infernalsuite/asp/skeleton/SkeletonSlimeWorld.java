package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.pdc.AdventurePersistentDataContainer;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SkeletonSlimeWorld implements SlimeWorld {

    private final String name;
    private final SlimeLoader loader;
    private final boolean readOnly;
    private final Map<Long, SlimeChunk> chunkStorage;
    private final ConcurrentMap<String, BinaryTag> extra;
    private final SlimePropertyMap propertyMap;
    private final int dataVersion;
    private final AdventurePersistentDataContainer extraPDC;

    public SkeletonSlimeWorld() {
        this.name = "skeleton";
        this.loader = null;
        this.readOnly = true;
        this.chunkStorage = new ConcurrentHashMap<>();
        this.extra = new ConcurrentHashMap<>();
        this.propertyMap = null;
        this.dataVersion = 0;
        this.extraPDC = new AdventurePersistentDataContainer(this.extra);
    }

    public SkeletonSlimeWorld(String name, SlimeLoader loader, boolean readOnly, Map<Long, SlimeChunk> chunkStorage, ConcurrentMap<String, BinaryTag> extra, SlimePropertyMap propertyMap, int dataVersion) {
        this.name = name;
        this.loader = loader;
        this.readOnly = readOnly;
        this.chunkStorage = chunkStorage != null ? chunkStorage : new ConcurrentHashMap<>();
        this.extra = extra != null ? extra : new ConcurrentHashMap<>();
        this.propertyMap = propertyMap;
        this.dataVersion = dataVersion;
        this.extraPDC = new AdventurePersistentDataContainer(this.extra);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public SlimeLoader getLoader() {
        return this.loader;
    }

    @Override
    public SlimeChunk getChunk(int x, int z) {
        return this.chunkStorage.get(chunkKey(x, z));
    }

    @Override
    public Collection<SlimeChunk> getChunkStorage() {
        return this.chunkStorage.values();
    }

    @Override
    public ConcurrentMap<String, BinaryTag> getExtraData() {
        return this.extra;
    }

    @Override
    public Collection<CompoundBinaryTag> getWorldMaps() {
        return null; // Skeleton doesn't have maps
    }

    @Override
    public SlimePropertyMap getPropertyMap() {
        return this.propertyMap;
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public SlimeWorld clone(String worldName) {
        return new SkeletonSlimeWorld();
    }

    @Override
    public SlimeWorld clone(String worldName, SlimeLoader loader) {
        return new SkeletonSlimeWorld();
    }

    @Override
    public int getDataVersion() {
        return this.dataVersion;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.extraPDC;
    }

    private static long chunkKey(int x, int z) {
        return (long) x << 32 | (z & 0xFFFFFFFFL);
    }
}
