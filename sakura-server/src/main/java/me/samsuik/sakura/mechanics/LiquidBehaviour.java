package me.samsuik.sakura.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LiquidBehaviour {
    public static boolean canLiquidSolidify(
        final Level level,
        final BlockPos pos,
        final FluidState fluidState,
        final MinecraftMechanicsTarget mechanicsTarget
    ) {
        // In legacy-paper and versions since 1.16, liquids should always solidify.
        if (mechanicsTarget.atLeast(MechanicVersion.v1_16) || mechanicsTarget.before(MechanicVersion.v1_10) && mechanicsTarget.isServerType(ServerType.PAPER)) {
            return true;
        }

        // In 1.13 and later, liquids can only solidify if they occupy at least half of the block.
        if (mechanicsTarget.atLeast(MechanicVersion.v1_13) && fluidState.getHeight(level, pos) >= 0.44444445f) {
            return true;
        }

        // todo: not sure if this is necessary, this looks identical to the condition above.
        if (mechanicsTarget.before(MechanicVersion.v1_13)) {
            return FlowingFluid.getLegacyLevel(fluidState) < 4;
        }

        return true;
    }
}
