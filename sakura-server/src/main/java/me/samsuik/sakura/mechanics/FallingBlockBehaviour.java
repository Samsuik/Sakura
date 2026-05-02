package me.samsuik.sakura.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FallingBlockBehaviour {
    public static boolean isAbleToStackOnBlock(final FallingBlockEntity fallingBlock, final MinecraftMechanicsTarget mechanicsTarget) {
        if (!mechanicsTarget.between(me.samsuik.sakura.mechanics.MechanicVersion.v1_9, me.samsuik.sakura.mechanics.MechanicVersion.v1_14)) {
            return true;
        }
        // This is patched by default on Paper.
        if (mechanicsTarget.isPaperOrDerivative()) {
            return true;
        }
        // todo: Entity#getOnPos might be a good alternative to this
        final BlockPos blockPos = BlockPos.containing(fallingBlock.getX(), fallingBlock.getY() - 0.001f, fallingBlock.getZ());
        final BlockState state = fallingBlock.level().getBlockState(blockPos);
        return !FallingBlock.isFree(state);
    }

    public static void removeBlockOnFall(final FallingBlockEntity fallingBlock, final Block block) {
        final Level level = fallingBlock.level();
        final BlockPos blockPos = fallingBlock.blockPosition();
        final BlockState state = level.getBlockState(blockPos);

        // todo: Do we need to call the event here? This event is already called in the fall method that spawns the falling block entity.
        if (state.is(block) && org.bukkit.craftbukkit.event.CraftEventFactory.callEntityChangeBlockEvent(fallingBlock, blockPos, Blocks.AIR.defaultBlockState())) {
            level.removeBlock(blockPos, false);
        } else {
            if (state.is(block)) {
                ((ServerLevel) level).getChunkSource().blockChanged(blockPos);
            }
            fallingBlock.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN);
        }
    }
}
