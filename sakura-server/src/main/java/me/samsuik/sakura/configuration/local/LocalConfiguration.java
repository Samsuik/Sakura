package me.samsuik.sakura.configuration.local;

import io.papermc.paper.util.MCUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.samsuik.sakura.configuration.local.ConfigurationContainer.SealedConfigurationContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.BoundingBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public final class LocalConfiguration implements LocalConfigurationAccessor {
    private static final CachedLocalConfiguration EMPTY_CONFIGURATION = CachedLocalConfiguration.emptyConfiguration();

    private final LocalConfigurationContainers containers = new LocalConfigurationContainers();
    private final Long2ObjectOpenHashMap<CachedLocalConfiguration> cachedConfiguration = new Long2ObjectOpenHashMap<>();
    private final CachedLocalConfiguration[] recentlyAccessed = new CachedLocalConfiguration[8];
    private long lastGameTime;
    private final Level level;

    public LocalConfiguration(final Level level) {
        this.level = level;
        Arrays.fill(this.recentlyAccessed, EMPTY_CONFIGURATION);
    }

    @Override
    public void set(final BoundingBox bb, final SealedConfigurationContainer container) {
        final ConfigurationArea area = new ConfigurationArea(bb);
        this.containers.add(area, container);

        // As containers are immutable, we just need to clear the cache to provide an immediate update.
        this.cachedConfiguration.clear();
        Arrays.fill(this.recentlyAccessed, EMPTY_CONFIGURATION);
    }

    @Override
    public @Nullable SealedConfigurationContainer remove(final BoundingBox bb) {
        final ConfigurationArea area = new ConfigurationArea(bb);
        final SealedConfigurationContainer container = this.containers.remove(area);

        this.cachedConfiguration.clear();
        Arrays.fill(this.recentlyAccessed, EMPTY_CONFIGURATION);
        return container;
    }

    @Override
    public @Nullable SealedConfigurationContainer get(final BoundingBox bb) {
        return this.containers.get(new ConfigurationArea(bb));
    }

    @Override
    public @Nullable ConfigurationContainer getContainer(final int x, final int y, final int z) {
        return this.containers.getContainer(x, y, z);
    }

    @Override
    public List<BoundingBox> getAreas(final int x, final int y, final int z) {
        return this.containers.getAreas(x, y, z).stream()
            .map(ConfigurationArea::asBoundingBox)
            .toList();
    }

    public CachedLocalConfiguration at(final Vec3 vec3) {
        return this.at(BlockPos.containing(vec3));
    }

    public CachedLocalConfiguration at(final BlockPos pos) {
        // This is sometimes called off the main thread when loading/generating chunks
        if (!MCUtil.isMainThread()) {
            return EMPTY_CONFIGURATION;
        }

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        final long sectionKey = ConfigurationArea.sectionKey(x, y, z, 2);
        final int recentCacheIndex = ((x & 1) << 2) | ((y & 1) << 1) | (z & 1);
        final CachedLocalConfiguration recentCache = recentlyAccessed[recentCacheIndex];

        // Fast path if the local configuration was recently accessed
        if (recentCache.sectionKey == sectionKey) {
            return recentCache;
        }

        // Clear the cache every minute
        final long gameTime = this.level.getGameTime();
        if (gameTime - this.lastGameTime >= 60 * 20) {
            this.cachedConfiguration.clear();
            this.lastGameTime = gameTime;
        }

        // Get the local configuration from the cache, if that isn't possible then create one
        CachedLocalConfiguration cache = this.cachedConfiguration.get(sectionKey);
        //noinspection ConstantValue
        if (cache == null) {
            final ConfigurationContainer container = this.getContainer(x, y, z);
            cache = new CachedLocalConfiguration(level, container, sectionKey);
            this.cachedConfiguration.put(sectionKey, cache);
        }

        this.recentlyAccessed[recentCacheIndex] = cache;
        return cache;
    }
}
