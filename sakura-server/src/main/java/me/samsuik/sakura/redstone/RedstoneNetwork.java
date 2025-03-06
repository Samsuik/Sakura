package me.samsuik.sakura.redstone;

import io.papermc.paper.configuration.WorldConfiguration;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.*;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.NullMarked;

import java.util.BitSet;
import java.util.List;

@NullMarked
public final class RedstoneNetwork {
    private final List<RedstoneWireUpdate> wireUpdates;
    private final List<BlockPos> updates;
    private final Object2ObjectMap<BlockPos, RedstoneOriginalPower> originalWirePower;
    private final LongArrayList listeners = new LongArrayList();
    private final BitSet redundantUpdates = new BitSet();
    private final TickExpiry expiry;

    public RedstoneNetwork(List<RedstoneWireUpdate> wireUpdates, List<BlockPos> updates, Object2ObjectMap<BlockPos, RedstoneOriginalPower> originalWirePower, TickExpiry expiry) {
        this.wireUpdates = new ObjectArrayList<>(wireUpdates);
        this.updates = new ObjectArrayList<>(updates);
        this.originalWirePower = new Object2ObjectOpenHashMap<>(originalWirePower);
        this.expiry = expiry;
    }

    private static boolean hasRedstoneComponentChanged(Level level, BlockPos pos, BlockState newBlock, BlockState oldBlock) {
        return newBlock.isRedstoneConductor(level, pos) != oldBlock.isRedstoneConductor(level, pos)
            || newBlock.isSignalSource() != oldBlock.isSignalSource();
    }

    private static boolean hasBlockChanged(Level level, BlockPos pos, BlockState newBlock, BlockState oldBlock) {
        return newBlock.getBlock() != oldBlock.getBlock();
    }

    public List<BlockPos> getWirePositions() {
        return this.wireUpdates.stream()
            .map(RedstoneWireUpdate::getPosition)
            .toList();
    }

    public TickExpiry getExpiry() {
        return this.expiry;
    }

    public boolean isRegistered() {
        return !this.listeners.isEmpty();
    }

    public boolean hasWire(BlockPos pos) {
        return this.originalWirePower.containsKey(pos);
    }

    public void invalidate(Level level) {
        for (long identifier : this.listeners) {
            level.blockChangeTracker.stopListening(identifier);
        }
        this.listeners.clear();
    }

    private void markNeighboringWiresForShapeUpdates(BlockPos pos, Object2ObjectMap<BlockPos, RedstoneWireUpdate> wires) {
        for (Direction direction : NeighborUpdater.UPDATE_ORDER) {
            BlockPos neighborPos = pos.relative(direction);
            RedstoneWireUpdate wireUpdate = wires.get(neighborPos);
            //noinspection ConstantValue
            if (wireUpdate != null) {
                wireUpdate.updateShapes();
            }
        }
    }

    public boolean prepareAndRegisterListeners(Level level, RedstoneNetworkSource networkSource) {
        Object2ObjectLinkedOpenHashMap<BlockPos, RedstoneWireUpdate> processedWires = new Object2ObjectLinkedOpenHashMap<>();
        boolean skipWireUpdates = networkSource.redstoneImplementation() != WorldConfiguration.Misc.RedstoneImplementation.VANILLA;
        for (RedstoneWireUpdate wireUpdate : this.wireUpdates.reversed()) {
            BlockPos wirePos = wireUpdate.getPosition();
            //noinspection ConstantValue
            if (processedWires.putAndMoveToFirst(wirePos, wireUpdate) == null) {
                // It's possible for the block below the redstone to break while the network is updating
                BlockState state = level.getBlockState(wirePos);
                if (state.is(Blocks.PISTON_HEAD)) {
                    return false;
                }
            } else if (skipWireUpdates && this.originalWirePower.get(wirePos).firstPower() != wireUpdate.getPower()) {
                // Filter out wires updates that are not the first and last update
                // This significantly reduces the amount of updates when unpowering
                wireUpdate.skipWireUpdate();
            }
        }

        for (int i = 0; i < this.updates.size(); ++i) {
            BlockPos updatePos = this.updates.get(i);
            BlockState state = level.getBlockState(updatePos);

            // Filter out redundant neighbor updates
            if (state.isAir() || state.liquid() || !state.isSpecialBlock() || processedWires.containsKey(updatePos)) {
                this.redundantUpdates.set(i);
            }

            // Look for blocks that actually need shape updates
            Block block = state.getBlock();
            if (state.is(Blocks.OBSERVER) || state.liquid() || block instanceof FallingBlock || block instanceof LiquidBlockContainer) {
                this.markNeighboringWiresForShapeUpdates(updatePos, processedWires);
            }
        }

        this.addBlockListeners(level);
        return true;
    }

    private void addBlockListeners(Level level) {
        ObjectOpenHashSet<BlockPos> positions = new ObjectOpenHashSet<>(this.updates);
        positions.addAll(this.originalWirePower.keySet());

        // Register block change listeners
        this.listeners.add(level.blockChangeTracker.listenForChangesOnce(
            RedstoneNetwork::hasRedstoneComponentChanged, positions, () -> this.invalidate(level)
        ));

        this.listeners.add(level.blockChangeTracker.listenForChangesOnce(
            RedstoneNetwork::hasBlockChanged, positions, this.redundantUpdates::clear
        ));
    }

    private boolean verifyWiresInNetwork(Level level) {
        for (Object2ObjectMap.Entry<BlockPos, RedstoneOriginalPower> wireEntry : this.originalWirePower.object2ObjectEntrySet()) {
            BlockState state = level.getBlockState(wireEntry.getKey());
            if (!state.is(Blocks.REDSTONE_WIRE)) {
                this.invalidate(level);
                return false;
            }

            if (state.getValue(RedStoneWireBlock.POWER) != wireEntry.getValue().originalPower()) {
                return false;
            }
        }

        return true;
    }

    private void performUpdates(Level level, Orientation orientation, RedStoneWireBlock wireBlock, int updateFrom, int updateTo) {
        for (int updateIndex = updateFrom; updateIndex < updateTo; ++updateIndex) {
            if (this.redundantUpdates.get(updateIndex)) {
                continue;
            }
            BlockPos updatePos = this.updates.get(updateIndex);
            level.getBlockState(updatePos).handleNeighborChanged(level, updatePos, wireBlock, orientation, false);
        }
    }

    public boolean applyFromCache(Level level) {
        this.expiry.refresh(level.getGameTime());
        if (!this.isRegistered() || !this.verifyWiresInNetwork(level)) {
            return false;
        }

        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, null, null);
        RedStoneWireBlock wireBlock = (RedStoneWireBlock) Blocks.REDSTONE_WIRE;
        int updateFrom = 0;

        for (RedstoneWireUpdate wireUpdate : this.wireUpdates) {
            if (wireUpdate.canSkipWireUpdate()) {
                updateFrom = wireUpdate.getUpdateIndex();
                continue;
            }

            int updateTo = wireUpdate.getUpdateIndex();
            this.performUpdates(level, orientation, wireBlock, updateFrom, updateTo);
            updateFrom = updateTo;

            BlockPos wirePos = wireUpdate.getPosition();
            BlockState state = level.getBlockState(wirePos);
            BlockState newState = state.setValue(RedStoneWireBlock.POWER, wireUpdate.getPower());
            if (level.setBlock(wirePos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE)) {
                if (wireUpdate.needsShapeUpdate()) {
                    wireBlock.turbo.updateNeighborShapes(level, wirePos, newState);
                }
            }
        }

        this.performUpdates(level, orientation, wireBlock, updateFrom, this.updates.size());
        return true;
    }
}
