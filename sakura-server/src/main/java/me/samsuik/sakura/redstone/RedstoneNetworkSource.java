package me.samsuik.sakura.redstone;

import io.papermc.paper.configuration.WorldConfiguration;
import me.samsuik.sakura.configuration.local.CachedLocalConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record RedstoneNetworkSource(WorldConfiguration.Misc.RedstoneImplementation redstoneImplementation,
                                    BlockPos position, @Nullable Orientation orientation,
                                    int updateDepth, int newPower, int oldPower) {

    public static RedstoneNetworkSource createNetworkSource(Level level, CachedLocalConfiguration localConfiguration, BlockPos pos,
                                                            @Nullable Orientation orientation, int newPower, int oldPower) {
        int updateDepth = level.neighborUpdater.getUpdateDepth();
        return new RedstoneNetworkSource(localConfiguration.paperRedstoneImplementation(), pos, orientation, updateDepth, newPower, oldPower);
    }
}
