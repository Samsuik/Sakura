package me.samsuik.sakura.redstone;

import io.papermc.paper.configuration.WorldConfiguration;
import me.samsuik.sakura.configuration.local.LocalValueConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record RedstoneNetworkSource(WorldConfiguration.Misc.RedstoneImplementation redstoneImplementation,
                                    BlockPos position, @Nullable Orientation orientation,
                                    int updateDepth, int newPower, int oldPower) {

    public static RedstoneNetworkSource createNetworkSource(Level level, LocalValueConfig localConfig, BlockPos pos,
                                                            @Nullable Orientation orientation, int newPower, int oldPower) {
        WorldConfiguration.Misc.RedstoneImplementation redstoneImplementation = localConfig.redstoneImplementation;
        int updateDepth = 0;
        return new RedstoneNetworkSource(redstoneImplementation, pos, orientation, updateDepth, newPower, oldPower);
    }
}
