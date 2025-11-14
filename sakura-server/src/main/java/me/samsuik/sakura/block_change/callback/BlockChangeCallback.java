package me.samsuik.sakura.block_change.callback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;

import java.util.function.LongConsumer;

@NullMarked
public interface BlockChangeCallback {
    static BlockChangeCallback identifier(final LongConsumer identifierConsumer) {
        return (pos, newBlock, oldBlock, identifier) -> identifierConsumer.accept(identifier);
    }

    void call(final BlockPos pos, final BlockState newBlock, final BlockState oldBlock, final long identifier);
}
