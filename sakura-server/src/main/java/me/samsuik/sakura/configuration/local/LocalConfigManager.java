package me.samsuik.sakura.configuration.local;

import ca.spottedleaf.concurrentutil.function.BiLongObjectConsumer;
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

public final class LocalConfigManager implements LocalStorageHandler {
    private static final long MASSIVE_REGION_SIZE = 0x10000000000L;
    private static final int SMALL_REGION_SIZE = 12;
    private static final int CONFIG_CACHE_EXPIRATION = 600;

    private final Map<LocalRegion, LocalValueStorage> storageMap = new Object2ObjectOpenHashMap<>();
    private final List<LocalRegion> largeRegions = new ObjectArrayList<>();
    private final Long2ObjectMap<List<LocalRegion>> smallRegions = new Long2ObjectOpenHashMap<>();
    private int regionExponent = 4;
    private final Long2ObjectMap<Pair<LocalValueConfig, TickExpiry>> chunkConfigCache = new Long2ObjectOpenHashMap<>();
    private final Level level;
    private long expirationTick = 0L;

    public LocalConfigManager(Level level) {
        this.level = level;
    }

    private int regionChunkCoord(int n) {
        return n >> this.regionExponent;
    }

    @Override
    public synchronized @NonNull Optional<LocalRegion> locate(int x, int z) {
        int regionX = this.regionChunkCoord(x);
        int regionZ = this.regionChunkCoord(z);
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
        boolean smallRegion = this.isSmallRegion(region);
        this.ensureRegionIsNotOverlapping(region, smallRegion);

        if (!smallRegion) {
            this.largeRegions.add(region);

            // The region exponent may be too small
            if ((this.largeRegions.size() & 15) == 0) {
                this.resizeRegions();
            }
        } else {
            this.forEachRegionChunks(region, this::addSmallRegion);
        }

        this.chunkConfigCache.clear();
        this.storageMap.put(region, storage);
    }

    @Override
    public synchronized void remove(@NonNull LocalRegion region) {
        this.forEachRegionChunks(region, (pos, r) -> {
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
        this.largeRegions.remove(region);
    }

    private void addSmallRegion(long pos, LocalRegion region) {
        this.smallRegions.computeIfAbsent(pos, k -> new ArrayList<>())
            .add(region);
    }

    private void forEachRegionChunks(LocalRegion region, BiLongObjectConsumer<LocalRegion> chunkConsumer) {
        int exponent = this.regionExponent;
        int minX = region.minX() >> exponent;
        int minZ = region.minZ() >> exponent;
        int maxX = region.maxX() >> exponent;
        int maxZ = region.maxZ() >> exponent;

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                chunkConsumer.accept(ChunkPos.asLong(x, z), region);
            }
        }
    }

    private void resizeRegions() {
        int newExponent = this.calculateRegionExponent();
        if (newExponent == this.regionExponent) {
            return; // nothing has changed
        }

        this.regionExponent = newExponent;
        this.largeRegions.clear();
        this.smallRegions.clear();

        for (LocalRegion region : this.storageMap.keySet()) {
            if (!this.isSmallRegion(region)) {
                this.largeRegions.add(region);
            } else {
                this.forEachRegionChunks(region, this::addSmallRegion);
            }
        }
    }

    private int calculateRegionExponent() {
        long totalRegionChunks = 0;
        for (LocalRegion region : this.storageMap.keySet()) {
            long chunks = regionChunks(region, 0);
            if (chunks >= MASSIVE_REGION_SIZE) {
                continue;
            }
            totalRegionChunks += chunks;
        }
        totalRegionChunks /= this.storageMap.size();

        int exponent = 4;
        while (true) {
            if ((totalRegionChunks >> exponent++) <= SMALL_REGION_SIZE / 2) {
                return exponent;
            }
        }
    }

    private boolean isSmallRegion(LocalRegion region) {
        return regionChunks(region, this.regionExponent) <= SMALL_REGION_SIZE;
    }

    private static long regionChunks(LocalRegion region, int exponent) {
        int sizeX = region.maxX() - region.minX() >> exponent;
        int sizeZ = region.maxZ() - region.minZ() >> exponent;
        return (long) (sizeX + 1) * (long) (sizeZ + 1);
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
        Pair<LocalValueConfig, TickExpiry> pair = this.chunkConfigCache.computeIfAbsent(chunkKey,
                k -> this.createLocalChunkConfigWithExpiry(position, gameTime));

        pair.value().refresh(gameTime);
        return pair.key();
    }

    private Pair<LocalValueConfig, TickExpiry> createLocalChunkConfigWithExpiry(BlockPos position, long gameTime) {
        // uses defaults from the sakura and paper config
        LocalValueConfig config = new LocalValueConfig(this.level);
        this.locate(position.getX(), position.getZ()).ifPresent(region -> {
            config.loadFromStorage(this.storageMap.get(region));
        });

        TickExpiry expiry = new TickExpiry(gameTime, CONFIG_CACHE_EXPIRATION);
        return Pair.of(config, expiry);
    }

    private void ensureRegionIsNotOverlapping(LocalRegion region, boolean smallRegion) {
        Set<LocalRegion> nearbyRegions = new ReferenceOpenHashSet<>();
        if (!smallRegion) {
            nearbyRegions.addAll(this.storageMap.keySet());
        } else {
            this.forEachRegionChunks(region, (pos, r) -> {
                nearbyRegions.addAll(this.smallRegions.getOrDefault(pos, List.of()));
            });
        }

        // Throw if any of the nearby regions are overlapping
        for (LocalRegion present : Iterables.concat(nearbyRegions, this.largeRegions)) {
            if (present != region && present.intersects(region)) {
                throw new OverlappingRegionException(present, region);
            }
        }
    }
}
