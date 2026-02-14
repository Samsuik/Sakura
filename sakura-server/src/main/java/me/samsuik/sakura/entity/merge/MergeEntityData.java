package me.samsuik.sakura.entity.merge;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MergeEntityData {
    private final Entity entity;
    private final List<MergeEntityData> connected = new ObjectArrayList<>();
    private int count = 1;
    private MergeLevel mergeLevel = MergeLevel.NONE;

    public MergeEntityData(Entity entity) {
        this.entity = entity;
    }

    private void updateEntityHandles(Entity entity) {
        for (MergeEntityData entityData : this.connected) {
            // entityData.entity.updateBukkitHandle(entity);
        }
    }

    public void mergeWith(@NotNull MergeEntityData mergeEntityData) {
        this.connected.add(mergeEntityData);
        this.connected.addAll(mergeEntityData.connected);
        this.count += mergeEntityData.getCount();
        mergeEntityData.updateEntityHandles(this.entity);
        mergeEntityData.setCount(0);
    }

    public LongOpenHashSet getOriginPositions() {
        LongOpenHashSet positions = new LongOpenHashSet();
        this.connected.forEach(entityData -> positions.add(entityData.entity.blockPosition().asLong()));
        return positions;
    }

    public boolean hasMerged() {
        return !this.connected.isEmpty() && this.count != 0;
    }

    public void setMergeLevel(MergeLevel mergeLevel) {
        this.mergeLevel = mergeLevel;
    }

    public MergeLevel getMergeLevel() {
        return mergeLevel;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
