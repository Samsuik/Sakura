package me.samsuik.sakura.redstone;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class RedstoneWireUpdate {
    private final BlockPos position;
    private final int power;
    private final int updateIndex;
    private boolean updateShape;
    private boolean skipWire;

    public RedstoneWireUpdate(final BlockPos position, final int power, final int updateIndex) {
        this.position = position;
        this.power = power;
        this.updateIndex = updateIndex;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getPower() {
        return this.power;
    }

    public int getUpdateIndex() {
        return this.updateIndex;
    }

    public boolean needsShapeUpdate() {
        return this.updateShape;
    }

    public void updateShapes() {
        this.updateShape = true;
    }

    public boolean canSkipWireUpdate() {
        return this.skipWire;
    }

    public void skipWireUpdate() {
        this.skipWire = true;
    }
}
