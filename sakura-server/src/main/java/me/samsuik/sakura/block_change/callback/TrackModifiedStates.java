package me.samsuik.sakura.block_change.callback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public final class TrackModifiedStates implements BlockChangeCallback {
    private final Map<BlockPos, BlockState> expectedStates = new HashMap<>();
    private int count = 0;

    public TrackModifiedStates(final Map<BlockPos, BlockState> states) {
        this.expectedStates.putAll(states);
    }

    public boolean isModified() {
        return this.count != 0;
    }

    @Override
    public void call(
        final BlockPos pos,
        final BlockState newBlock,
        final BlockState oldBlock,
        final long identifier
    ) {
        final BlockState expectedState = this.expectedStates.get(pos);
        if (expectedState == newBlock) {
            this.count--;
        } else if (expectedState == oldBlock) {
            this.count++;
        }
    }
}
