package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.api.world.SlimeChunkSection;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

import java.util.List;
import java.util.Map;

public class SlimeChunkSkeleton {

    private final int x;
    private final int z;
    private final SlimeChunkSection[] sections;
    private final CompoundBinaryTag heightMaps;
    private final List<CompoundBinaryTag> biomes;
    private final List<CompoundBinaryTag> blockLight;
    private final Map<String, BinaryTag> tileEntities;
    private final Integer[] light;
    private final CompoundBinaryTag upgradeData;
    private final ListBinaryTag carvingMasks;
    private final ListBinaryTag entities;

    public SlimeChunkSkeleton(int x, int z, SlimeChunkSection[] sections, CompoundBinaryTag heightMaps, List<CompoundBinaryTag> biomes, List<CompoundBinaryTag> blockLight, Map<String, BinaryTag> tileEntities, Integer[] light, CompoundBinaryTag upgradeData, ListBinaryTag carvingMasks, ListBinaryTag entities) {
        this.x = x;
        this.z = z;
        this.sections = sections;
        this.heightMaps = heightMaps;
        this.biomes = biomes;
        this.blockLight = blockLight;
        this.tileEntities = tileEntities;
        this.light = light;
        this.upgradeData = upgradeData;
        this.carvingMasks = carvingMasks;
        this.entities = entities;
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public SlimeChunkSection[] getSections() {
        return sections;
    }

    public CompoundBinaryTag getHeightMaps() {
        return heightMaps;
    }

    public List<CompoundBinaryTag> getBiomes() {
        return biomes;
    }

    public List<CompoundBinaryTag> getBlockLight() {
        return blockLight;
    }

    public Map<String, BinaryTag> getTileEntities() {
        return tileEntities;
    }

    public Integer[] getLight() {
        return light;
    }

    public CompoundBinaryTag getUpgradeData() {
        return upgradeData;
    }

    public ListBinaryTag getCarvingMasks() {
        return carvingMasks;
    }

    public ListBinaryTag getEntities() {
        return entities;
    }
}
