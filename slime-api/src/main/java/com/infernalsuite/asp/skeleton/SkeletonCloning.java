package com.infernalsuite.asp.skeleton;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.nbt.BinaryTag;

public class SkeletonCloning {

    public static SlimeWorld clone(SlimeWorld world) {
        return new SkeletonSlimeWorld();
    }

    public static SlimeWorld weakCopy(SlimeWorld world) {
        return new SkeletonSlimeWorld();
    }

    public static SlimeWorld fullClone(String worldName, SlimeWorld world, SlimeLoader loader, boolean readOnly) {
        Map<Long, SlimeChunk> clonedChunks = new java.util.concurrent.ConcurrentHashMap<>();
        for (SlimeChunk chunk : world.getChunkStorage()) {
            long pos = com.infernalsuite.asp.Util.chunkPosition(chunk.getX(), chunk.getZ());
            clonedChunks.put(pos, chunk); // Shallow copy for now
        }
        ConcurrentMap<String, BinaryTag> clonedExtra = new java.util.concurrent.ConcurrentHashMap<>(world.getExtraData());
        return new SkeletonSlimeWorld(worldName, loader, readOnly, clonedChunks, clonedExtra, world.getPropertyMap(), world.getDataVersion());
    }
}
