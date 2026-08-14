package me.samsuik.sakura.configuration.local;

import io.papermc.paper.util.MCUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.samsuik.sakura.configuration.local.ConfigurationContainer.SealedConfigurationContainer;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.BoundingBox;
import org.jspecify.annotations.Nullable;

import java.util.*;

@SuppressWarnings("ConstantValue")
public final class LocalConfigurationManager implements LocalConfigurationAccessor {
    private final Map<ConfigurationBounds, SealedConfigurationContainer> containers = new HashMap<>();
    private final ConfigurationSpatialMap configurationSpatialMap = new ConfigurationSpatialMap();
    private final Level level;
    private final SealedConfigurationContainer defaultContainer;
    private long lastGameTime;
    private final Long2ObjectOpenHashMap<CachedLocalConfiguration> cachedConfiguration = new Long2ObjectOpenHashMap<>();
    private final CachedLocalConfiguration[] recentlyAccessed = new CachedLocalConfiguration[8];

    public LocalConfigurationManager(final Level level) {
        this.level = level;
        this.defaultContainer = ConfigurationDefaults.levelDefaults(level);
        Arrays.fill(this.recentlyAccessed, new CachedLocalConfiguration(Integer.MIN_VALUE, this.defaultContainer));
    }

    @Override
    public void set(final BoundingBox bb, final SealedConfigurationContainer container) {
        final ConfigurationBounds bounds = ConfigurationBounds.fromBukkitBoundingBox(bb);

        this.containers.put(bounds, container);
        this.configurationSpatialMap.add(bounds);
        this.clearCache();
    }

    @Override
    public @Nullable SealedConfigurationContainer remove(final BoundingBox bb) {
        final ConfigurationBounds bounds = ConfigurationBounds.fromBukkitBoundingBox(bb);
        final SealedConfigurationContainer oldContainer = this.containers.remove(bounds);
        if (oldContainer != null) {
            this.configurationSpatialMap.remove(bounds);
        }

        this.clearCache();
        return oldContainer;
    }

    @Override
    public @Nullable SealedConfigurationContainer get(final BoundingBox bb) {
        return this.containers.get(ConfigurationBounds.fromBukkitBoundingBox(bb));
    }

    @Override
    public @Nullable ConfigurationContainer getContainer(final int x, final int y, final int z) {
        final ConfigurationContainer newContainer = new ConfigurationContainer();
        final List<ConfigurationBounds> bounds = this.configurationSpatialMap.get(x, y, z);
        bounds.sort(Comparator.comparingLong(ConfigurationBounds::volume));

        for (final ConfigurationBounds configurationBounds : bounds) {
            newContainer.fillAbsentValues(this.containers.get(configurationBounds));
        }

        return newContainer.contents().isEmpty() ? null : newContainer;
    }

    public ConfigurationContainer getContainerWithDefaults(final int x, final int y, final int z) {
        final ConfigurationContainer container = this.getContainer(x, y, z);
        if (container == null) {
            return this.defaultContainer;
        }

        container.fillAbsentValues(this.defaultContainer);
        return container;
    }

    @Override
    public List<BoundingBox> getAreas(final int x, final int y, final int z) {
        return this.configurationSpatialMap.get(x, y, z).stream()
            .map(ConfigurationBounds::asBukkitBoundingBox)
            .toList();
    }

    @Override
    public List<BoundingBox> getAllAreas() {
        return this.containers.keySet().stream()
            .map(ConfigurationBounds::asBukkitBoundingBox)
            .toList();
    }

    @Override
    public void removeAll() {
        this.containers.clear();
        this.configurationSpatialMap.clear();
        this.clearCache();
    }

    private void clearCache() {
        this.cachedConfiguration.clear();
        Arrays.fill(this.recentlyAccessed, new CachedLocalConfiguration(Integer.MIN_VALUE, this.defaultContainer));
    }

    public CachedLocalConfiguration at(final Vec3 vec3) {
        final int x = Mth.floor(vec3.x());
        final int y = Mth.floor(vec3.y());
        final int z = Mth.floor(vec3.z());

        return this.getCachedContainer(x, y, z);
    }

    public CachedLocalConfiguration at(final Vec3i pos) {
        return this.getCachedContainer(pos.getX(), pos.getY(), pos.getZ());
    }

    private CachedLocalConfiguration getCachedContainer(final int x, final int y, final int z) {
        if (!MCUtil.isMainThread()) {
            return new CachedLocalConfiguration(Integer.MIN_VALUE, this.defaultContainer);
        }

        final long sectionKey = SectionPos.asLong(x, y, z);
        final int recentCacheIndex = ((x & 1) << 2) | ((y & 1) << 1) | (z & 1);
        final CachedLocalConfiguration recentCache = recentlyAccessed[recentCacheIndex];
        if (recentCache.sectionKey == sectionKey) {
            return recentCache;
        }

        final long gameTime = this.level.getGameTime();
        if (gameTime - this.lastGameTime >= 60 * 20) {
            this.cachedConfiguration.clear();
            this.lastGameTime = gameTime;
        }

        CachedLocalConfiguration cache = this.cachedConfiguration.get(sectionKey);
        if (cache == null) {
            final ConfigurationContainer container = this.getContainerWithDefaults(x, y, z);
            cache = new CachedLocalConfiguration(sectionKey, container);
            this.cachedConfiguration.put(sectionKey, cache);
        }

        this.recentlyAccessed[recentCacheIndex] = cache;
        return cache;
    }
}
