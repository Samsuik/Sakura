package me.samsuik.sakura.entity.merge;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public final class MergeEntityData {
    private final Entity entity;
    private final List<MergeEntityData> connected = new ObjectArrayList<>();
    public int count = 1;
    public MergeLevel mergeLevel = MergeLevel.NONE;

    public MergeEntityData(Entity entity) {
        this.entity = entity;
    }

    private void updateEntityHandles(Entity entity) {
        for (MergeEntityData entityData : this.connected) {
            entityData.entity.updateBukkitHandle(entity);
        }
    }

    public void mergeWith(MergeEntityData mergeEntityData) {
        this.connected.add(mergeEntityData);
        this.connected.addAll(mergeEntityData.connected);
        this.count += mergeEntityData.count;
        mergeEntityData.updateEntityHandles(this.entity);
        mergeEntityData.count = 0;
        mergeEntityData.connected.clear();
    }

    public boolean hasMerged() {
        return !this.connected.isEmpty() && this.count != 0;
    }

    public LongOpenHashSet getOriginPositions() {
        LongOpenHashSet positions = new LongOpenHashSet();
        this.connected.forEach(entityData -> positions.add(entityData.entity.getPackedOriginPosition()));
        return positions;
    }
}
