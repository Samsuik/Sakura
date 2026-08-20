package me.samsuik.sakura.event;

import me.samsuik.sakura.event.block.BlockPreDispenseEvent;
import me.samsuik.sakura.event.entity.PreSpawnerTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SakuraEvents {
    public static int preDispense(final LevelAccessor level, final BlockPos pos, final int delay) {
        final Block block = CraftBlock.at(level, pos);
        final BlockPreDispenseEvent event = new BlockPreDispenseEvent(block, delay);
        event.callEvent();

        return Math.max(event.getDelay(), 1);
    }

    public static boolean preSpawnerTick(final BlockPos pos, final ServerLevel level) {
        final Location location = CraftLocation.toBukkit(pos, level);
        final PreSpawnerTickEvent event = new PreSpawnerTickEvent(location);
        return event.callEvent();
    }
}
