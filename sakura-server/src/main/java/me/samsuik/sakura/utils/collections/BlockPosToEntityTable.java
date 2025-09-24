package me.samsuik.sakura.utils.collections;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@NullMarked
public final class BlockPosToEntityTable {
    private final @Nullable Entity[] entities;
    private final int mask;

    public BlockPosToEntityTable(final int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("Table size cannot be negative");
        } else {
            final int size = HashCommon.nextPowerOfTwo(expectedSize - 1);
            this.entities = new Entity[size];
            this.mask = (size - 1);
        }
    }

    public @Nullable Entity get(final BlockPos blockPos) {
        return this.entities[this.key(blockPos)];
    }

    public @Nullable Entity put(final Entity entity) {
        return this.put(entity.blockPosition(), entity);
    }

    public @Nullable Entity put(final BlockPos blockPos, final @Nullable Entity entity) {
        final int index = this.key(blockPos);
        final Entity present = this.entities[index];
        this.entities[index] = entity;
        return present;
    }

    public @Nullable Entity remove(final BlockPos blockPos) {
        return this.put(blockPos, null);
    }

    public void clear() {
        Arrays.fill(this.entities, null);
    }

    private int key(final BlockPos blockPos) {
        return blockPos.hashCode() & this.mask;
    }
}
