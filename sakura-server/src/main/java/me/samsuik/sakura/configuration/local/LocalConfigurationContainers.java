package me.samsuik.sakura.configuration.local;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static me.samsuik.sakura.configuration.local.ConfigurationContainer.*;

@NullMarked
public final class LocalConfigurationContainers {
    private static final long MASSIVE_REGION_SIZE = 0x180000000L;
    private static final int LARGE_AREA_THRESHOLD = 6 * 6 * 6;
    private static final Comparator<ConfigurationArea> AREA_BY_VOLUME = Comparator.comparingLong(ConfigurationArea::volume);

    private final Map<ConfigurationArea, SealedConfigurationContainer> containers = new HashMap<>();
    private final List<ConfigurationArea> largeAreas = new ArrayList<>();
    private final Long2ObjectOpenHashMap<List<ConfigurationArea>> smallAreas = new Long2ObjectOpenHashMap<>();
    private int sectionExponent = 4;
    private int changes = 0;

    private boolean isLargeArea(final ConfigurationArea area) {
        return area.countSections(this.sectionExponent) > LARGE_AREA_THRESHOLD;
    }

    public void add(final ConfigurationArea area, final SealedConfigurationContainer container) {
        final ConfigurationContainer presentContainer = this.containers.put(area, container);
        if (presentContainer == null) {
            if (this.isLargeArea(area)) {
                this.largeAreas.add(area);
            } else {
                this.updateSections(area, false);
            }

            if ((changes++ & 15) == 0) {
                this.resizeSections();
            }
        }
    }

    public @Nullable SealedConfigurationContainer remove(final ConfigurationArea area) {
        final SealedConfigurationContainer container = this.containers.remove(area);
        if (this.isLargeArea(area)) {
            this.largeAreas.remove(area);
        } else if (container != null) {
            this.updateSections(area, true);
        }

        if ((changes++ & 15) == 0) {
            this.resizeSections();
        }

        return container;
    }

    public @Nullable SealedConfigurationContainer get(final ConfigurationArea area) {
        return this.containers.get(area);
    }

    public @Nullable ConfigurationContainer getContainer(final int x, final int y, final int z) {
        final ConfigurationContainer newContainer = new ConfigurationContainer();
        final List<ConfigurationArea> areas = this.getAreas(x, y, z);
        areas.sort(AREA_BY_VOLUME); // sorted by size

        for (final ConfigurationArea area : areas) {
            if (area.contains(x, y, z)) {
                newContainer.fillAbsentValues(this.containers.get(area));
            }
        }

        return newContainer.contents().isEmpty() ? null : newContainer;
    }

    public List<ConfigurationArea> getAreas(final int x, final int y, final int z) {
        final long sectionKey = ConfigurationArea.sectionKey(x, y, z, this.sectionExponent);
        final List<ConfigurationArea> nearby = this.smallAreas.getOrDefault(sectionKey, Collections.emptyList());
        final List<ConfigurationArea> foundAreas = new ArrayList<>();

        for (final ConfigurationArea area : Iterables.concat(nearby, this.largeAreas)) {
            if (area.contains(x, y, z)) {
                foundAreas.add(area);
            }
        }

        return foundAreas;
    }

    private int calculateNewSectionExponent() {
        long totalSectionCount = 0;
        int totalAreas = 0;
        for (final ConfigurationArea area : this.containers.keySet()) {
            final long sections = area.countSections(0);
            if (sections < MASSIVE_REGION_SIZE) {
                totalSectionCount += sections;
                totalAreas++;
            }
        }

        final long averageSectionCount = totalSectionCount / Math.max(totalAreas, 1);
        for (int exponent = 4;; exponent++) {
            if ((averageSectionCount >> exponent) < LARGE_AREA_THRESHOLD) {
                return exponent;
            }
        }
    }

    private void resizeSections() {
        final int newExponent = this.calculateNewSectionExponent();
        if (newExponent == this.sectionExponent) {
            return; // nothing has changed
        }

        this.sectionExponent = newExponent;
        this.smallAreas.clear();
        this.largeAreas.clear();

        for (final ConfigurationArea area : this.containers.keySet()) {
            if (this.isLargeArea(area)) {
                this.largeAreas.add(area);
            } else {
                this.updateSections(area, false);
            }
        }
    }

    private void updateSections(final ConfigurationArea area, final boolean remove) {
        area.forEach(this.sectionExponent, sectionKey -> {
            if (remove) {
                final List<ConfigurationArea> areas = this.smallAreas.get(sectionKey);
                //noinspection ConstantValue
                if (areas != null) {
                    areas.remove(area);
                    if (areas.isEmpty()) {
                        this.smallAreas.remove(sectionKey);
                    }
                }
            } else {
                this.smallAreas.computeIfAbsent(sectionKey, k -> new ArrayList<>()).add(area);
            }
        });
    }
}
