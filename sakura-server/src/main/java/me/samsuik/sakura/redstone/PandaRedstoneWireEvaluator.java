package me.samsuik.sakura.redstone;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is a PandaWire implementation for modern Minecraft.
 * <p>
 * Not to be confused with {@link net.minecraft.world.level.redstone.ExperimentalRedstoneWireEvaluator}
 *
 * @see <a href="https://gist.github.com/Panda4994/70ed6d39c89396570e062e4404a8d518">The original Pandawire gist</a>
 */
@NullMarked
public final class PandaRedstoneWireEvaluator extends RedstoneWireEvaluator {
    /*
    * I considered replacing the lists with LinkedHashSets for faster lookup,
    * but an artificial test showed barely any difference in performance
    */
    /** Positions that need to be turned off **/
    private final ArrayDeque<BlockPos> turnOff = new ArrayDeque<>();
    /** Positions that need to be checked to be turned on **/
    private final ArrayDeque<BlockPos> turnOn = new ArrayDeque<>();
    /** Positions of wire that was updated already (Ordering determines update order and is therefore required!) **/
    private final LinkedHashSet<BlockPos> updatedRedstoneWire = new LinkedHashSet<>();

    /** Ordered arrays of the facings; Needed for the update order.
     *  I went with a vertical-first order here, but vertical last would work to.
     *  However it should be avoided to update the vertical axis between the horizontal ones as this would cause unneeded directional behavior. **/
    private static final Direction[] FACINGS_HORIZONTAL = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
    private static final Direction[] FACINGS_VERTICAL = {Direction.DOWN, Direction.UP};
    private static final Direction[] FACINGS = ArrayUtils.addAll(FACINGS_VERTICAL, FACINGS_HORIZONTAL);

    /** Offsets for all surrounding blocks that need to receive updates **/
    private static final Vec3i[] SURROUNDING_BLOCKS_OFFSET;
    static {
        final Set<Vec3i> set = Sets.newLinkedHashSet();
        for (final Direction facing : FACINGS) {
            set.add(facing.getUnitVec3i());
        }

        final Set<Vec3i> offsets = Sets.newLinkedHashSet(set);
        for (final Vec3i neighborOffset : offsets) {
            for (final Vec3i adjacentOffset : offsets) {
                set.add(neighborOffset.offset(adjacentOffset));
            }
        }

        set.remove(Vec3i.ZERO);
        SURROUNDING_BLOCKS_OFFSET = set.toArray(new Vec3i[0]);
    }
    
    public PandaRedstoneWireEvaluator(final RedStoneWireBlock wireBlock) {
        super(wireBlock);
    }

    @Override
    public void updatePowerStrength(
        final Level level,
        final BlockPos pos,
        final BlockState state,
        final @Nullable Orientation orientation,
        final boolean updateShape
    ) {
        // Recalculate the connected wires
        this.calculateCurrentChanges(level, pos, level.getBlockState(pos));

        // Set to collect all the updates, to only execute them once. Ordering required.
        final Set<BlockPos> blocksNeedingUpdate = Sets.newLinkedHashSet();

        // Add the needed updates
        for (final BlockPos updatedWirePos : this.updatedRedstoneWire) {
            this.addBlocksNeedingUpdate(level, updatedWirePos, blocksNeedingUpdate);
        }

        // Add all other updates to keep known behaviors
        // They are added in a backwards order because it preserves a commonly used behavior with the update order
        for (final BlockPos updatedWirePos : this.updatedRedstoneWire.reversed()) {
            this.addAllSurroundingBlocks(updatedWirePos, blocksNeedingUpdate);
        }

        // Remove updates on the wires as they just were updated
        blocksNeedingUpdate.removeAll(this.updatedRedstoneWire);

        /*
         * Avoid unnecessary updates on the just updated wires
         * A huge scale test showed about 40% more ticks per second
         * It's probably less in normal usage but likely still worth it
         */
        this.updatedRedstoneWire.clear();

        // Execute updates
        for (final BlockPos neighborPos : blocksNeedingUpdate) {
            level.neighborChanged(neighborPos, this.wireBlock, null);
        }
    }

    /**
     * Turns on or off all connected wires
     *
     * @param level	      World
     * @param sourcePos	  Position of the wire that received the update
     * @param sourceState Current state of this block
     */
    private void calculateCurrentChanges(final Level level, final BlockPos sourcePos, final BlockState sourceState) {
        // Turn off all connected wires first if needed
        if (sourceState.is(this.wireBlock)) {
            this.turnOff.add(sourcePos);
        } else {
            // In case this wire was removed, check the surrounding wires
            this.checkSurroundingWires(level, sourcePos);
        }

        while (!this.turnOff.isEmpty()) {
            final BlockPos pos = this.turnOff.poll();
            final BlockState state = level.getBlockState(pos);
            final int oldPower = state.getValue(RedStoneWireBlock.POWER);
            final int blockPower = this.getBlockSignal(level, pos);
            final int wirePower = this.getIncomingWireSignal(level, pos);
            final int newPower = Math.max(blockPower, wirePower);

            // Power lowered?
            if (newPower < oldPower) {
                // If it's still powered by a direct source (but weaker) mark for turn on
                if (blockPower > 0 && !this.turnOn.contains(pos)) {
                    this.turnOn.add(pos);
                }
                // Set all the way to off for now, because wires that were powered by this need to update first
                this.setWireState(level, pos, state, 0);
                // Power rose?
            } else if (newPower > oldPower) {
                // Set new Power
                this.setWireState(level, pos, state, newPower);
            }
            // Check if surrounding wires need to change based on the current/new state and add them to the lists
            this.checkSurroundingWires(level, pos);
        }

        while (!this.turnOn.isEmpty()) {
            final BlockPos pos = this.turnOn.poll();
            final BlockState state = level.getBlockState(pos);
            final int oldPower = state.getValue(RedStoneWireBlock.POWER);
            final int blockPower = this.getBlockSignal(level, pos);
            final int wirePower = this.getIncomingWireSignal(level, pos);
            int newPower = Math.max(blockPower, wirePower);

            if (oldPower != newPower) {
                final BlockRedstoneEvent event = new BlockRedstoneEvent(CraftBlock.at(level, pos), oldPower, newPower);
                newPower = event.callEvent() ? event.getNewCurrent() : oldPower;
            }

            if (newPower > oldPower) {
                this.setWireState(level, pos, state, newPower);
            }
            // Check if surrounding wires need to change based on the current/new state and add them to the lists
            this.checkSurroundingWires(level, pos);
        }

        this.turnOff.clear();
    }

    /**
     * Checks if an wire needs to be marked for update depending on the power next to it
     *
     * @author panda
     *
     * @param level      World
     * @param pos		 Position of the wire that might need to change
     * @param otherPower Power of the wire next to it
     */
    private void addWireToList(final Level level, final BlockPos pos, final int otherPower) {
        final BlockState state = level.getBlockState(pos);
        if (state.is(this.wireBlock)) {
            final int power = state.getValue(RedStoneWireBlock.POWER);
            // Could get powered stronger by the neighbor?
            if (power < otherPower - 1 && !this.turnOn.contains(pos)) {
                // Mark for turn on check.
                this.turnOn.add(pos);
            }
            // Should have powered the neighbor? Probably was powered by it and is in turn off phase.
            if (power > otherPower && !this.turnOff.contains(pos)) {
                // Mark for turn off check.
                this.turnOff.add(pos);
            }
        }
    }

    /**
     * Checks if the wires around need to get updated depending on this wires state.
     * Checks all wires below before the same layer before on top to keep
     * some more rotational symmetry around the y-axis.
     *
     * @author panda
     *
     * @param level	World
     * @param pos	Position of the wire
     */
    private void checkSurroundingWires(final Level level, final BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        int ownPower = 0;
        if (state.is(this.wireBlock)) {
            ownPower = state.getValue(RedStoneWireBlock.POWER);
        }
        // Check wires on the same layer first as they appear closer to the wire
        for (final Direction facingHorizontal : FACINGS_HORIZONTAL) {
            this.addWireToList(level, pos.relative(facingHorizontal), ownPower);
        }
        for (final Direction facingVertical : FACINGS_VERTICAL) {
            final BlockPos offsetPos = pos.relative(facingVertical);
            final boolean solidBlock = level.getBlockState(offsetPos).isRedstoneConductor(level, offsetPos);
            for (final Direction facingHorizontal : FACINGS_HORIZONTAL) {
                final BlockPos adjacentPos = offsetPos.relative(facingHorizontal);
                // wire can travel upwards if the block on top doesn't cut the wire (is non-solid)
                // it can travel down if the block below is solid and the block "diagonal" doesn't cut off the wire (is non-solid)
                if (facingVertical == Direction.UP && !solidBlock) {
                    this.addWireToList(level, adjacentPos, ownPower);
                } else if (facingVertical == Direction.DOWN && solidBlock && !level.getBlockState(adjacentPos).isRedstoneConductor(level, adjacentPos)) {
                    this.addWireToList(level, adjacentPos, ownPower);
                }
            }
        }
    }

    /**
     * Adds all blocks that need to receive an update from a redstone change in this position.
     * This means only blocks that actually could change.
     *
     * @author panda
     *
     * @param level	World
     * @param pos	Position of the wire
     * @param set	Set to add the update positions too
     */
    private void addBlocksNeedingUpdate(final Level level, final BlockPos pos, final Set<BlockPos> set) {
        final Set<Direction> connectedSides = this.getSidesToPower(level, pos);
        // Add the blocks next to the wire first (closest first order)
        for (final Direction facing : FACINGS) {
            final BlockPos offsetPos = pos.relative(facing);
            final BlockState state = level.getBlockState(offsetPos);
            // canConnectTo() is not the nicest solution here as it returns true for e.g. the front of a repeater
            // canBlockBePowereFromSide catches these cases
            final boolean flag = connectedSides.contains(facing.getOpposite()) || facing == Direction.DOWN;
            if (flag || (facing.getAxis().isHorizontal() && RedStoneWireBlock.shouldConnectTo(state, facing))) {
                if (canBlockBePoweredFromSide(state, facing, true)) {
                    set.add(offsetPos);
                }
            }

            // Later add blocks around the surrounding blocks that get powered
            if (flag && state.isRedstoneConductor(level, offsetPos)) {
                for (final Direction adjacentFacing : FACINGS) {
                    final BlockPos adjacentPos = offsetPos.relative(adjacentFacing);
                    if (canBlockBePoweredFromSide(level.getBlockState(adjacentPos), adjacentFacing, false)) {
                        set.add(adjacentPos);
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a block can get powered from a side.
     * This behavior would better be implemented per block type as follows:
     *  - return false as default. (blocks that are not affected by redstone don't need to be updated, it doesn't really hurt if they are either)
     *  - return true for all blocks that can get powered from all side and change based on it (doors, fence gates, trap doors, note blocks, lamps, dropper, hopper, TNT, rails, possibly more)
     *  - implement own logic for pistons, repeaters, comparators and redstone torches
     *  The current implementation was chosen to keep everything in one class.
     * <p>
     *  Why is this extra check needed?
     *  1. It makes sure that many old behaviors still work (QC + Pistons).
     *  2. It prevents updates from "jumping".
     *     Or rather it prevents this wire to update a block that would get powered by the next one of the same line.
     *     This is to prefer as it makes understanding the update order of the wire really easy. The signal "travels" from the power source.
     *
     * @author panda
     *
     * @param state		State of the block
     * @param side		Side from which it gets powered
     * @param isWire	True if it's powered by a wire directly, False if through a block
     * @return			True if the block can change based on the power level it gets on the given side, false otherwise
     */
    private boolean canBlockBePoweredFromSide(final BlockState state, final Direction side, final boolean isWire) {
        Block block = state.getBlock();
        if (state.isAir()) return false;
        if (block instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.FACING) == side.getOpposite()) return false;
        if (block instanceof DiodeBlock && state.getValue(DiodeBlock.FACING) != side.getOpposite())
            return isWire && block instanceof ComparatorBlock && state.getValue(ComparatorBlock.FACING).getAxis() != side.getAxis() && side.getAxis().isHorizontal();
        return !(state.getBlock() instanceof RedstoneWallTorchBlock) || (!isWire && state.getValue(RedstoneWallTorchBlock.FACING) == side);
    }

    /**
     * Creates a list of all horizontal sides that can get powered by a wire.
     * The list is ordered the same as the facingsHorizontal.
     *
     * @param level	World
     * @param pos   Position of the wire
     * @return		List of all facings that can get powered by this wire
     */
    private Set<Direction> getSidesToPower(final Level level, final BlockPos pos) {
        final Set<Direction> sidesToPower = new HashSet<>();
        final BlockState state = level.getBlockState(pos);
        for (final Direction direction : FACINGS_HORIZONTAL) {
            final RedstoneSide side = state.getValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(direction));
            if (side.isConnected()) {
                sidesToPower.add(direction);
            }
        }

        return sidesToPower;
    }

    /**
     * Adds all surrounding positions to a set.
     * This is the neighbor blocks, as well as their neighbors
     *
     * @param pos
     * @param set
     */
    private void addAllSurroundingBlocks(final BlockPos pos, final Set<BlockPos> set) {
        for (final Vec3i vect : SURROUNDING_BLOCKS_OFFSET) {
            set.add(pos.offset(vect));
        }
    }

    /**
     * Sets the block state of a wire with a new power level and marks for updates
     *
     * @author panda
     *
     * @param level	World
     * @param pos	Position at which the state needs to be set
     * @param state	Old state
     * @param power	Power it should get set to
     */
    private void setWireState(final Level level, final BlockPos pos, final BlockState state, final int power) {
        final BlockState newState = state.setValue(RedStoneWireBlock.POWER, power);
        level.setBlock(pos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        this.updatedRedstoneWire.add(pos);
    }
}
