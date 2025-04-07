package me.samsuik.sakura.configuration.local;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.samsuik.sakura.local.LocalRegion;
import me.samsuik.sakura.local.storage.LocalStorageHandler;
import me.samsuik.sakura.local.storage.LocalValueStorage;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.LongConsumer;

public final class LocalConfigManager implements LocalStorageHandler {
    private static final int SMALL_REGION_SIZE = 12;
    private static final int CONFIG_CACHE_EXPIRATION = 600;

    private final Map<LocalRegion, LocalValueStorage> storageMap = new Object2ObjectOpenHashMap<>();
    private final List<LocalRegion> largeRegions = new ObjectArrayList<>();
    private final Long2ObjectMap<List<LocalRegion>> smallRegions = new Long2ObjectOpenHashMap<>();
    private int regionExponent = 0;
    private final Long2ObjectMap<Pair<LocalValueConfig, TickExpiry>> chunkConfigCache = new Long2ObjectOpenHashMap<>();
    private final Level level;
    private long expirationTick = 0L;

    public LocalConfigManager(Level level) {
        this.level = level;
    }

    @Override
    public synchronized @NonNull Optional<LocalRegion> locate(int x, int z) {
        int regionX = x >> this.regionExponent;
        int regionZ = z >> this.regionExponent;
        long regionPos = ChunkPos.asLong(regionX, regionZ);
        List<LocalRegion> regions = this.smallRegions.getOrDefault(regionPos, List.of());
        for (LocalRegion region : Iterables.concat(regions, this.largeRegions)) {
            if (region.contains(x, z)) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized @Nullable LocalValueStorage get(@NonNull LocalRegion region) {
        return this.storageMap.get(region);
    }

    @Override
    public synchronized boolean has(@NonNull LocalRegion region) {
        return this.storageMap.containsKey(region);
    }

    @Override
    public synchronized void put(@NonNull LocalRegion region, @NonNull LocalValueStorage storage) {
        int shift = this.regionExponent;
        int regionChunks = regionChunks(region, shift);

        // make sure there's no overlapping regions
        this.ensureRegionIsNotOverlapping(region, regionChunks);

        if (regionChunks <= SMALL_REGION_SIZE) {
            this.forEachRegionChunks(region, pos -> {
                this.smallRegions.computeIfAbsent(pos, k -> new ArrayList<>())
                    .add(region);
            });
        } else {
            this.largeRegions.add(region);

            // The region exponent may be too small
            if ((this.largeRegions.size() & 15) == 0) {
                this.resizeRegions();
            }
        }

        this.chunkConfigCache.clear();
        this.storageMap.put(region, storage);
    }

    @Override
    public synchronized void remove(@NonNull LocalRegion region) {
        this.forEachRegionChunks(region, pos -> {
            List<LocalRegion> regions = this.smallRegions.get(pos);
            if (regions != null) {
                regions.remove(region);
                if (regions.isEmpty()) {
                    this.smallRegions.remove(pos);
                }
            }
        });

        this.chunkConfigCache.clear();
        this.storageMap.remove(region);
    }

    private void forEachRegionChunks(LocalRegion region, LongConsumer chunkConsumer) {
        int exponent = this.regionExponent;
        int minX = region.minX() >> exponent;
        int minZ = region.minZ() >> exponent;
        int maxX = region.maxX() >> exponent;
        int maxZ = region.maxZ() >> exponent;

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                chunkConsumer.accept(ChunkPos.asLong(x, z));
            }
        }
    }

    private void resizeRegions() {
        List<LocalRegion> regions = this.regions();
        int newExponent = this.calculateRegionExponent(regions);
        if (newExponent == this.regionExponent) {
            return; // nothing has changed
        }

        this.regionExponent = newExponent;
        this.largeRegions.clear();
        this.smallRegions.clear();

        for (LocalRegion region : regions) {
            int regionChunks = regionChunks(region, newExponent);
            if (regionChunks <= SMALL_REGION_SIZE) {
                this.forEachRegionChunks(region, pos -> {
                    this.smallRegions.computeIfAbsent(pos, k -> new ArrayList<>())
                        .add(region);
                });
            } else {
                this.largeRegions.add(region);
            }
        }
    }

    private int calculateRegionExponent(List<LocalRegion> regions) {
        int regionChunks = 0;
        for (LocalRegion region : regions) {
            regionChunks += regionChunks(region, 0);
        }
        regionChunks /= regions.size();

        int exponent = 4;
        while (true) {
            if ((regionChunks >> exponent++) <= SMALL_REGION_SIZE / 2) {
                return exponent;
            }
        }
    }

    private static int regionChunks(LocalRegion region, int exponent) {
        int sizeX = region.maxX() - region.minX() >> exponent;
        int sizeZ = region.maxZ() - region.minZ() >> exponent;
        return (sizeX + 1) * (sizeZ + 1);
    }

    @Override
    public synchronized @NonNull List<LocalRegion> regions() {
        return new ArrayList<>(this.storageMap.keySet());
    }

    public synchronized LocalValueConfig config(BlockPos position) {
        long gameTime = this.level.getGameTime();
        long ticks = gameTime - this.expirationTick;
        if (ticks >= CONFIG_CACHE_EXPIRATION / 3) {
            this.chunkConfigCache.values().removeIf(pair -> pair.value().isExpired(gameTime));
            this.expirationTick = gameTime;
        }

        long chunkKey = ChunkPos.asLong(position.getX() >> 4, position.getZ() >> 4);
        Pair<LocalValueConfig, TickExpiry> pair = this.chunkConfigCache.computeIfAbsent(chunkKey, k -> {
            return Pair.of(this.createLocalChunkConfig(position), new TickExpiry(gameTime, CONFIG_CACHE_EXPIRATION));
        });

        pair.value().refresh(gameTime);
        return pair.key();
    }

    private LocalValueConfig createLocalChunkConfig(BlockPos position) {
        // uses defaults from the sakura config
        LocalValueConfig config = new LocalValueConfig(this.level);
        this.locate(position.getX(), position.getZ()).ifPresent(region -> {
            config.loadFromStorage(this.storageMap.get(region));
        });
        return config;
    }

    private void ensureRegionIsNotOverlapping(LocalRegion region, int regionChunks) {
        Set<LocalRegion> nearbyRegions = new ReferenceOpenHashSet<>();
        if (regionChunks > SMALL_REGION_SIZE) {
            nearbyRegions.addAll(this.storageMap.keySet());
        } else {
            this.forEachRegionChunks(region, pos -> {
                nearbyRegions.addAll(this.smallRegions.getOrDefault(pos, List.of()));
            });
        }
        for (LocalRegion present : Iterables.concat(nearbyRegions, this.largeRegions)) {
            if (present != region && present.intersects(region)) {
                throw new OverlappingRegionException(present, region);
            }
        }
    }
}
