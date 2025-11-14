package me.samsuik.sakura.redstone.cache;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.*;
import me.samsuik.sakura.block_change.BlockFilter;
import me.samsuik.sakura.block_change.BlockStateChangeTracker;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
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
    private final List<BlockPos> neighborUpdates;
    private final Object2ObjectMap<BlockPos, RedstoneOriginalPower> originalWirePower;
    private final LongArrayList listeners = new LongArrayList();
    private final BitSet redundantUpdates = new BitSet();
    private final TickExpiry expiry;

    public RedstoneNetwork(
        final List<RedstoneWireUpdate> wireUpdates,
        final List<BlockPos> neighborUpdates,
        final Object2ObjectMap<BlockPos, RedstoneOriginalPower> originalWirePower,
        final TickExpiry expiry
    ) {
        this.wireUpdates = new ObjectArrayList<>(wireUpdates);
        this.neighborUpdates = new ObjectArrayList<>(neighborUpdates);
        this.originalWirePower = new Object2ObjectOpenHashMap<>(originalWirePower);
        this.expiry = expiry;
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

    public boolean hasWire(final BlockPos pos) {
        return this.originalWirePower.containsKey(pos);
    }

    public void invalidate(final BlockStateChangeTracker tracker) {
        for (final long identifier : this.listeners) {
            tracker.stopListening(identifier);
        }
        this.listeners.clear();
    }

    private void markNeighboringWiresForShapeUpdates(final BlockPos pos, final Object2ObjectMap<BlockPos, RedstoneWireUpdate> wires) {
        for (final Direction direction : NeighborUpdater.UPDATE_ORDER) {
            final BlockPos neighborPos = pos.relative(direction);
            final RedstoneWireUpdate wireUpdate = wires.get(neighborPos);
            //noinspection ConstantValue
            if (wireUpdate != null) {
                wireUpdate.updateShapes();
            }
        }
    }

    public boolean prepareAndRegisterListeners(final Level level, final RedstoneNetworkSource networkSource) {
        final Object2ObjectLinkedOpenHashMap<BlockPos, RedstoneWireUpdate> processedWires = new Object2ObjectLinkedOpenHashMap<>();
        final boolean skipWireUpdates = networkSource.isVanilla();

        for (final RedstoneWireUpdate wireUpdate : this.wireUpdates.reversed()) {
            final BlockPos wirePos = wireUpdate.getPosition();
            //noinspection ConstantValue
            if (processedWires.putAndMoveToFirst(wirePos, wireUpdate) == null) {
                // It's possible for the block below the redstone to break while the network is updating
                final BlockState blockStateBelow = level.getBlockState(wirePos.below());
                if (blockStateBelow.is(Blocks.PISTON_HEAD) || blockStateBelow.is(BlockTags.TRAPDOORS)) {
                    return false;
                }
            } else if (skipWireUpdates && this.originalWirePower.get(wirePos).firstPower() != wireUpdate.getPower()) {
                // Filter out wires updates that are not the first and last update
                // This significantly reduces the amount of updates when unpowering
                wireUpdate.skipWireUpdate();
            }
        }

        for (int updateIndex = 0; updateIndex < this.neighborUpdates.size(); ++updateIndex) {
            final BlockPos updatePos = this.neighborUpdates.get(updateIndex);
            final BlockState state = level.getBlockState(updatePos);
            final Block block = state.getBlock();

            // Never apply updates to redstone wires
            if (state.is(Blocks.REDSTONE_WIRE)) {
                this.neighborUpdates.set(updateIndex, null);
                continue;
            }

            // Filter out redundant neighbor updates
            if (state.isAir() || state.liquid() || !state.isSpecialBlock()) {
                this.redundantUpdates.set(updateIndex);
            }

            // Look for blocks that actually need shape updates
            if (state.is(Blocks.OBSERVER) || state.liquid() || block instanceof FallingBlock || block instanceof LiquidBlockContainer) {
                this.markNeighboringWiresForShapeUpdates(updatePos, processedWires);
            }
        }

        this.addBlockListeners(level);
        return true;
    }

    private void allowRedundantNeighborUpdates() {
        for (int updateIndex = 0; updateIndex < this.neighborUpdates.size(); ++updateIndex) {
            if (!this.redundantUpdates.get(updateIndex)) {
                continue;
            }
            final BlockPos pos = this.neighborUpdates.get(updateIndex);
            if (!this.originalWirePower.containsKey(pos)) {
                this.redundantUpdates.clear(updateIndex);
            }
        }
    }

    private void addBlockListeners(final Level level) {
        final ObjectOpenHashSet<BlockPos> positions = new ObjectOpenHashSet<>(this.neighborUpdates);
        positions.addAll(this.originalWirePower.keySet());
        positions.remove(null);

        // Register block change listeners
        final BlockStateChangeTracker tracker = level.blockStateChangeTracker;
        this.listeners.add(tracker.firstChange(
            positions,
            BlockFilter.REDSTONE_COMPONENT,
            () -> this.invalidate(tracker)
        ));

        this.listeners.add(tracker.firstChange(
            positions,
            BlockFilter.BLOCK_TYPE,
            this::allowRedundantNeighborUpdates
        ));
    }

    private boolean verifyWiresInNetwork(final Level level) {
        for (final Object2ObjectMap.Entry<BlockPos, RedstoneOriginalPower> wireEntry : this.originalWirePower.object2ObjectEntrySet()) {
            final BlockState state = level.getBlockState(wireEntry.getKey());
            if (!state.is(Blocks.REDSTONE_WIRE)) {
                this.invalidate(level.blockStateChangeTracker);
                return false;
            }

            if (state.getValue(RedStoneWireBlock.POWER) != wireEntry.getValue().originalPower()) {
                return false;
            }
        }

        return true;
    }

    private void performUpdates(
        final Level level,
        final Orientation orientation,
        final RedStoneWireBlock wireBlock,
        final int updateFrom,
        final int updateTo
    ) {
        for (int updateIndex = updateFrom; updateIndex < updateTo; ++updateIndex) {
            if (this.redundantUpdates.get(updateIndex)) {
                continue;
            }
            final BlockPos updatePos = this.neighborUpdates.get(updateIndex);
            //noinspection ConstantValue
            if (updatePos != null) {
                level.getBlockState(updatePos).handleNeighborChanged(level, updatePos, wireBlock, orientation, false);
            }
        }
    }

    public boolean applyFromCache(final Level level) {
        this.expiry.refresh(level.getGameTime());
        if (!this.isRegistered() || !this.verifyWiresInNetwork(level)) {
            return false;
        }

        final Orientation defaultOrientation = ExperimentalRedstoneUtils.initialOrientation(level, null, null);
        final RedStoneWireBlock wireBlock = (RedStoneWireBlock) Blocks.REDSTONE_WIRE;
        int updateFrom = 0;

        // Apply all wire updates in the network
        for (final RedstoneWireUpdate wireUpdate : this.wireUpdates) {
            if (wireUpdate.canSkipWireUpdate()) {
                updateFrom = wireUpdate.getUpdateIndex();
                continue;
            }

            final int updateTo = wireUpdate.getUpdateIndex();
            this.performUpdates(level, defaultOrientation, wireBlock, updateFrom, updateTo);
            updateFrom = updateTo;

            final BlockPos wirePos = wireUpdate.getPosition();
            final BlockState state = level.getBlockState(wirePos);
            final BlockState newState = state.setValue(RedStoneWireBlock.POWER, wireUpdate.getPower());

            // Update the wire power and apply shape updates when needed
            if (level.setBlock(wirePos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE)) {
                if (wireUpdate.needsShapeUpdate()) {
                    wireBlock.turbo.updateNeighborShapes(level, wirePos, newState);
                }
            }
        }

        this.performUpdates(level, defaultOrientation, wireBlock, updateFrom, this.neighborUpdates.size());
        return true;
    }
}
