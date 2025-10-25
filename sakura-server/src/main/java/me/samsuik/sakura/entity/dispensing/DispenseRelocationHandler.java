package me.samsuik.sakura.entity.dispensing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Adjusts the dispense position when using the Sake mechanics server type.
 * <p>
 * This attempts to replicate the behaviour found on Kore jars (Wine and Tequila).
 */
public final class DispenseRelocationHandler {
    private final Level level;
    private final Map<BlockPos, DispenserGroup> groups = new HashMap<>();

    public DispenseRelocationHandler(final Level level) {
        this.level = level;
    }

    public BlockPos relocatePosition(final BlockPos pos, final BlockPos sourcePos, final BlockState sourceState) {
        DispenserGroup group = this.groups.get(sourcePos);
        if (group == null) {
            final Set<BlockPos> connected = this.getConnectedDispensers(sourcePos, sourceState);
            final DispenserGroup newGroup = new DispenserGroup();
            connected.forEach(dispenserPos -> groups.put(dispenserPos, newGroup));
            group = newGroup;
        }

        return group.getSpawnPosition(pos);
    }

    public void clear() {
        this.groups.clear();
    }

    private Set<BlockPos> getConnectedDispensers(final BlockPos pos, final BlockState state) {
        // from testing in-game the expected behaviour is as follows:
        // it checks horizontally then vertically for dispensers that meet the conditions
        // - must be facing the same direction & powered
        // - must be scheduled in the same tick
        // - the block in front or the block below cannot have a collision shape
        // - must have tnt in the first occupied slot
        final Set<BlockPos> dispensers = new HashSet<>(List.of(pos));
        final Direction facing = state.getValue(DispenserBlock.FACING);

        // if the source doesn't meet the conditions then don't search
        if (!this.doesDispenserMeetConditions(pos, state, facing, true)) {
            return dispensers;
        }

        for (final Direction horizontalDirection : facing.getClockWise().getAxis().getDirections()) {
            for (int distance = 1; distance < 64; ++distance) {
                final BlockPos adjacentPos = pos.relative(horizontalDirection, distance);
                final BlockState adjacentState = this.level.getBlockState(adjacentPos);

                if (state != adjacentState || !this.doesDispenserMeetConditions(adjacentPos, adjacentState, facing, false)) {
                    break;
                }

                dispensers.add(adjacentPos);
            }
        }

        for (final BlockPos horizontalPos : List.copyOf(dispensers)) {
            for (final Direction verticalDirection : Direction.Plane.VERTICAL) {
                for (int distance = 1; distance < 384; ++distance) {
                    final BlockPos adjacentPos = horizontalPos.relative(verticalDirection, distance);
                    final BlockState adjacentState = this.level.getBlockState(adjacentPos);

                    if (state != adjacentState || !this.doesDispenserMeetConditions(adjacentPos, adjacentState, facing, false)) {
                        break;
                    }

                    dispensers.add(adjacentPos);
                }
            }
        }

        return dispensers;
    }

    private boolean doesDispenserMeetConditions(final BlockPos pos, final BlockState state, final Direction facing, final boolean source) {
        if (!source && !this.level.getBlockTicks().willTickThisTick(pos, state.getBlock())) {
            return false;
        }

        final BlockState stateInFront = this.level.getBlockState(pos.relative(facing));
        final BlockState stateBeneath = this.level.getBlockState(pos.relative(facing).below());

        if (!stateInFront.getCollisionShape(this.level, pos).isEmpty() && !stateBeneath.getCollisionShape(this.level, pos).isEmpty()) {
            return false;
        }

        final Container container = (Container) this.level.getBlockEntity(pos);
        if (container == null) {
            return false; // we should never get here, but you never know with FAWE
        }

        for (int slot = 0; slot < container.getContainerSize(); ++slot) {
            if (!isSlotEmptyOrHasTnt(container.getItem(slot))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSlotEmptyOrHasTnt(final ItemStack itemstack) {
        return itemstack.isEmpty() || itemstack.is(Items.TNT);
    }
}
