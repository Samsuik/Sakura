package me.samsuik.sakura.redstone;

import it.unimi.dsi.fastutil.objects.*;
import me.samsuik.sakura.configuration.local.CachedLocalConfiguration;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@NullMarked
public final class RedstoneWireCache {
    private final Map<RedstoneNetworkSource, RedstoneNetwork> networkCache = new Object2ObjectOpenHashMap<>();
    private @Nullable RedstoneNetworkSource networkSource;
    private final List<RedstoneWireUpdate> wireUpdates = new ObjectArrayList<>();
    private final List<BlockPos> updates = new ObjectArrayList<>();
    private final Object2ObjectMap<BlockPos, RedstoneOriginalPower> originalWirePower = new Object2ObjectOpenHashMap<>();
    private final Level level;
    private @Nullable RedstoneNetwork updatingNetwork;

    public RedstoneWireCache(Level level) {
        this.level = level;
    }

    public Map<RedstoneNetworkSource, RedstoneNetwork> getNetworkCache() {
        return this.networkCache;
    }

    public boolean isWireUpdating(BlockPos pos) {
        return this.updatingNetwork != null && this.updatingNetwork.hasWire(pos);
    }

    private boolean isTrackingWireUpdates() {
        return this.networkSource != null;
    }

    public boolean tryApplyFromCache(BlockPos pos, @Nullable Orientation orientation, int newPower, int oldPower) {
        final CachedLocalConfiguration localConfiguration = this.level.localConfig().at(pos);
        if (!localConfiguration.redstoneBehaviour.cache() || this.isTrackingWireUpdates()) {
            return false;
        }

        // Ignore any wire changes while updating the network.
        if (this.updatingNetwork != null) {
            return true;
        }

        RedstoneNetworkSource networkSource = RedstoneNetworkSource.createNetworkSource(this.level, localConfiguration, pos, orientation, newPower, oldPower);
        RedstoneNetwork network = this.networkCache.get(networkSource);
        if (network != null) {
            try {
                this.updatingNetwork = network;
                return network.applyFromCache(this.level);
            } finally {
                this.updatingNetwork = null;
            }
        } else {
            // Start tracking power and neighbor updates
            this.networkSource = networkSource;
            return false;
        }
    }

    public void trackWirePower(BlockPos pos, int newPower, int oldPower) {
        if (this.isTrackingWireUpdates()) {
            this.originalWirePower.putIfAbsent(pos, new RedstoneOriginalPower(oldPower, newPower));
            this.wireUpdates.add(new RedstoneWireUpdate(pos, newPower, this.updates.size()));
        }
    }

    public void trackNeighbor(BlockPos pos) {
        if (this.isTrackingWireUpdates()) {
            this.updates.add(pos);
        }
    }

    public void trackNeighborsAt(BlockPos pos) {
        if (this.isTrackingWireUpdates()) {
            for (Direction neighbor : NeighborUpdater.UPDATE_ORDER) {
                this.updates.add(pos.relative(neighbor));
            }
        }
    }

    public void expire(long tick) {
        this.networkCache.values().removeIf(network -> {
            if (network.getExpiry().isExpired(tick)) {
                network.invalidate(this.level);
                return true;
            }
            return false;
        });
    }

    public void stopTracking() {
        if (!this.isTrackingWireUpdates()) {
            return;
        }

        // Cache expires if it has not been used in 600 ticks
        TickExpiry expiration = new TickExpiry(this.level.getGameTime(), 600);
        RedstoneNetwork redstoneNetwork = new RedstoneNetwork(
            this.wireUpdates, this.updates, this.originalWirePower, expiration
        );

        if (redstoneNetwork.prepareAndRegisterListeners(this.level, this.networkSource)) {
            this.networkCache.put(this.networkSource, redstoneNetwork);
        }

        this.wireUpdates.clear();
        this.updates.clear();
        this.originalWirePower.clear();
        this.networkSource = null;
    }
}
