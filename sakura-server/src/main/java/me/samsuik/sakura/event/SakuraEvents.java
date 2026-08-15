package me.samsuik.sakura.event;

import me.samsuik.sakura.event.block.BlockPreDispenseEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SakuraEvents {
    public static int preDispense(final LevelAccessor level, final BlockPos pos, final int delay) {
        final Block block = CraftBlock.at(level, pos);
        final BlockPreDispenseEvent event = new BlockPreDispenseEvent(block, delay);
        event.callEvent();

        return Math.min(event.getDelay(), 1);
    }
}
