package me.samsuik.sakura.redstone.cache;

import it.unimi.dsi.fastutil.objects.*;
import me.samsuik.sakura.configuration.local.CachedLocalConfiguration;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.ApiStatus;
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

    public RedstoneWireCache(final Level level) {
        this.level = level;
    }

    @ApiStatus.Internal
    public Map<RedstoneNetworkSource, RedstoneNetwork> getNetworkCache() {
        return this.networkCache;
    }

    public boolean isApplyingCache() {
        return this.updatingNetwork != null;
    }

    public boolean isUpdatingRedstoneWire(final BlockPos pos) {
        return this.updatingNetwork != null && this.updatingNetwork.hasWire(pos);
    }

    private boolean isTrackingWireUpdates() {
        return this.networkSource != null;
    }

    public void trackWirePower(final BlockPos pos, final int newPower, final int oldPower) {
        if (this.isTrackingWireUpdates()) {
            this.originalWirePower.putIfAbsent(pos, new RedstoneOriginalPower(oldPower, newPower));
            this.wireUpdates.add(new RedstoneWireUpdate(pos, newPower, this.updates.size()));
        }
    }

    public void trackNeighbor(final BlockPos pos) {
        if (this.isTrackingWireUpdates()) {
            this.updates.add(pos);
        }
    }

    public void trackNeighborsAt(final BlockPos pos) {
        if (this.isTrackingWireUpdates()) {
            for (final Direction neighbor : NeighborUpdater.UPDATE_ORDER) {
                this.updates.add(pos.relative(neighbor));
            }
        }
    }

    public boolean applyFromCache(final BlockPos pos, final @Nullable Orientation orientation, final int newPower, final int oldPower) {
        final CachedLocalConfiguration localConfiguration = this.level.localConfig().at(pos);
        if (!localConfiguration.redstoneBehaviour.cache() || this.isTrackingWireUpdates()) {
            return false;
        }

        final RedstoneNetworkSource networkSource = RedstoneNetworkSource.createNetworkSource(
            this.level, localConfiguration, pos, orientation, newPower, oldPower
        );
        final RedstoneNetwork network = this.networkCache.get(networkSource);

        // Try to apply a network cache if one does not exist then start tracking wire updates
        if (network != null) {
            try {
                this.updatingNetwork = network;
                return network.applyFromCache(this.level);
            } finally {
                this.updatingNetwork = null;
                this.networkSource = null; // applying a cache while tracking can cause issues
            }
        } else {
            // Start tracking wire updates
            this.networkSource = networkSource;
            return false;
        }
    }

    public void stopTracking() {
        if (!this.isTrackingWireUpdates()) {
            return;
        }

        // The cache will expire if it has not been used in 600 ticks
        final TickExpiry expiration = new TickExpiry(this.level.getGameTime(), 600);
        final RedstoneNetwork redstoneNetwork = new RedstoneNetwork(
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

    public void expire(final long tick) {
        this.networkCache.values().removeIf(network -> {
            if (network.getExpiry().isExpired(tick)) {
                network.invalidate(this.level.blockStateChangeTracker);
                return true;
            }

            return false;
        });
    }
}
