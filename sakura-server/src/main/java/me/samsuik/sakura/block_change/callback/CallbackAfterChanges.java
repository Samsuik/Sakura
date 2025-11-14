package me.samsuik.sakura.block_change.callback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;

import java.util.function.LongConsumer;

@NullMarked
public final class CallbackAfterChanges implements BlockChangeCallback {
    private final int count;
    private int changes = 0;
    private final LongConsumer callback;

    public CallbackAfterChanges(final int count, final LongConsumer callback) {
        this.count = count;
        this.callback = callback;
    }

    public void reset() {
        this.changes = 0;
    }

    @Override
    public void call(
        final BlockPos pos,
        final BlockState newBlock,
        final BlockState oldBlock,
        final long identifier
    ) {
        if (++this.changes >= this.count) {
            this.callback.accept(identifier);
        }
    }
}
