package me.samsuik.sakura.block_change;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import me.samsuik.sakura.block_change.callback.BlockChangeCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ListeningToPositions(
    ObjectImmutableList<BlockPos> positions,
    long identifier,
    BlockFilter filter,
    BlockChangeCallback callback
) {
    public void onChange(final Level level, final BlockPos pos, final BlockState newBlock, final BlockState oldBlock) {
        if (this.filter.test(level, pos, newBlock, oldBlock)) {
            this.callback.call(pos, newBlock, oldBlock, this.identifier);
        }
    }
}
