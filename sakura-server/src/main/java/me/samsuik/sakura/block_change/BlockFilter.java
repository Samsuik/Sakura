package me.samsuik.sakura.block_change;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface BlockFilter {
    BlockFilter ANY = (level, pos, newBlock, oldBlock) -> true;

    BlockFilter BLOCK_TYPE = (level, pos, newBlock, oldBlock) -> !oldBlock.is(newBlock.getBlock());

    BlockFilter MOVING_BLOCK = (level, pos, newBlock, oldBlock) -> newBlock.is(Blocks.MOVING_PISTON);

    BlockFilter COLLISION = (level, pos, newBlock, oldBlock) -> {
        if (oldBlock.is(newBlock.getBlock())) {
            return false;
        }

        final VoxelShape oldShape = oldBlock.getCollisionShape(level, pos);
        final VoxelShape newShape = newBlock.getCollisionShape(level, pos);
        return !Shapes.equal(oldShape, newShape);
    };

    BlockFilter REDSTONE_COMPONENT = (level, pos, oldBlock, newBlock) -> {
        if (oldBlock.is(newBlock.getBlock())) {
            return false;
        }

        return newBlock.isRedstoneConductor(level, pos) != oldBlock.isRedstoneConductor(level, pos)
            || newBlock.isSignalSource() != oldBlock.isSignalSource();
    };

    boolean test(final Level level, final BlockPos pos, final BlockState newBlock, final BlockState oldBlock);
}
