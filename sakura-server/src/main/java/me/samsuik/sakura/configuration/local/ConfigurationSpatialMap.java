package me.samsuik.sakura.configuration.local;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.LongConsumer;

public final class ConfigurationSpatialMap {
    private static final int COORD_BITS = 21;
    private static final int COORD_MASK = (1 << COORD_BITS) - 1;

    private static final int OVERSAMPLE_THRESHOLD = Short.MAX_VALUE;
    private static final int OVERSAMPLE_SIZE = 2;
    private static final int MAX_EXPECTED_CELLS = 4;
    private static final int DEFAULT_CELL_SIZE = MAX_EXPECTED_CELLS + OVERSAMPLE_SIZE;

    private final Long2ObjectOpenHashMap<@Nullable List<ConfigurationBounds>> cells = new Long2ObjectOpenHashMap<>();
    private final Set<ConfigurationBounds> allBounds = new HashSet<>();
    private final List<ConfigurationBounds> largeBounds = new ArrayList<>();
    private final int[] sizeCounts = new int[32];
    private int cellSize = DEFAULT_CELL_SIZE;
    private int lbMod;

    public void add(final ConfigurationBounds bounds) {
        if (!this.allBounds.add(bounds)) {
            return;
        }

        final int size = requiredSize(bounds.largestXZAxis());
        this.sizeCounts[size]++;

        if (this.canFitInsideCells(bounds)) {
            this.addToCells(bounds);
            return;
        }

        this.largeBounds.add(bounds);

        if ((++this.lbMod & 31) == 0) {
            this.resize();
        }
    }

    public void remove(final ConfigurationBounds bounds) {
        if (!this.allBounds.remove(bounds)) {
            return;
        }

        final int size = requiredSize(bounds.largestXZAxis());
        this.sizeCounts[size]--;

        if (this.canFitInsideCells(bounds)) {
            this.removeFromCells(bounds);
        } else {
            this.largeBounds.remove(bounds);
        }

        if ((++this.lbMod & 31) == 0) {
            this.resize();
        }

        if (this.allBounds.isEmpty()) {
            this.cellSize = DEFAULT_CELL_SIZE;
        }
    }

    public List<ConfigurationBounds> get(final int x, final int y, final int z) {
        final List<ConfigurationBounds> results = new ObjectArrayList<>(2);
        for (final ConfigurationBounds bounds : this.largeBounds) {
            if (bounds.contains(x, y, z)) {
                results.add(bounds);
            }
        }

        final long cellCoord = cellCoord(x, z);
        final List<ConfigurationBounds> bounds = this.cells.get(cellCoord);
        if (bounds != null) {
            for (final ConfigurationBounds bound : bounds) {
                if (bound.contains(x, y, z)) {
                    results.add(bound);
                }
            }
        }

        return results;
    }

    public void clear() {
        this.cells.clear();
        this.allBounds.clear();
        this.largeBounds.clear();
        Arrays.fill(this.sizeCounts, 0);
        this.cellSize = DEFAULT_CELL_SIZE;
    }

    private boolean canFitInsideCells(final ConfigurationBounds bounds) {
        return (bounds.largestXZAxis() >> this.cellSize) <= MAX_EXPECTED_CELLS;
    }

    private int toCellCoord(final int num) {
        return Math.floorDiv(num, 1 << this.cellSize);
    }

    private long cellCoord(final int x, final int z) {
        return coord(this.toCellCoord(x), this.toCellCoord(z));
    }

    private static long coord(final int x, final int z) {
        return (long) (x & COORD_MASK) | (long) (z & COORD_MASK) << COORD_BITS;
    }

    private void resize() {
        final int bestCellSize = this.bestCellSize();
        if (this.cellSize == bestCellSize) {
            return;
        }

        this.cellSize = bestCellSize;
        this.cells.clear();
        this.largeBounds.clear();

        for (final ConfigurationBounds bound : this.allBounds) {
            if (!this.canFitInsideCells(bound)) {
                this.largeBounds.add(bound);
            } else {
                this.addToCells(bound);
            }
        }
    }

    private static int requiredSize(final int num) {
        if (num < 1) {
            return DEFAULT_CELL_SIZE;
        }
        return Math.clamp(32 - Integer.numberOfLeadingZeros(num - 1), DEFAULT_CELL_SIZE, 31);
    }

    private int bestCellSize() {
        int bestSize = DEFAULT_CELL_SIZE;
        int maxCount = 0;

        for (int size = 0; size < this.sizeCounts.length; size++) {
            if (this.sizeCounts[size] > maxCount) {
                maxCount = this.sizeCounts[size];
                bestSize = size;
            }
        }

        for (int size = bestSize + 1; size < this.sizeCounts.length; size++) {
            if (this.sizeCounts[size] >= maxCount / 2) {
                bestSize = size;
            }
        }

        if (this.allBounds.size() > OVERSAMPLE_THRESHOLD) {
            bestSize += OVERSAMPLE_SIZE;
        }

        return Math.clamp(bestSize, DEFAULT_CELL_SIZE, COORD_BITS);
    }

    private void addToCells(final ConfigurationBounds bounds) {
        this.forEach(bounds, coord -> {
            List<ConfigurationBounds> cellBounds = this.cells.get(coord);
            if (cellBounds == null) {
                cellBounds = new ObjectArrayList<>(2);
                this.cells.put(coord, cellBounds);
            }

            cellBounds.add(bounds);
        });
    }

    private void removeFromCells(final ConfigurationBounds bounds) {
        this.forEach(bounds, coord -> {
            final List<ConfigurationBounds> cellBounds = this.cells.get(coord);
            if (cellBounds != null) {
                cellBounds.remove(bounds);

                if (cellBounds.isEmpty()) {
                    this.cells.remove(coord);
                }
            }
        });
    }

    private void forEach(final ConfigurationBounds bounds, final LongConsumer consumer) {
        final int minX = this.toCellCoord(bounds.minX());
        final int minZ = this.toCellCoord(bounds.minZ());

        final int maxX = this.toCellCoord(bounds.maxX());
        final int maxZ = this.toCellCoord(bounds.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                consumer.accept(coord(x, z));
            }
        }
    }
}
