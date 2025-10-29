package me.samsuik.sakura.configuration.local;

import net.minecraft.util.Mth;
import org.bukkit.util.BoundingBox;
import org.jspecify.annotations.NullMarked;

import java.util.function.LongConsumer;

@NullMarked
public record ConfigurationArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public ConfigurationArea(final BoundingBox boundingBox) {
        this(
            Mth.floor(boundingBox.getMinX()),
            Mth.floor(boundingBox.getMinY()),
            Mth.floor(boundingBox.getMinZ()),
            Mth.floor(boundingBox.getMaxX()),
            Mth.floor(boundingBox.getMaxY()),
            Mth.floor(boundingBox.getMaxZ())
        );
    }

    public BoundingBox asBoundingBox() {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public boolean contains(final int x, final int y, final int z) {
        return x >= this.minX && x <= this.maxX
            && y >= this.minY && y <= this.maxY
            && z >= this.minZ && z <= this.maxZ;
    }

    public long volume() {
        return this.countSections(0);
    }

    public long countSections(final int sectionExponent) {
        final int sectionsX = ((this.maxX - this.minX) >> sectionExponent) + 1;
        final int sectionsY = ((this.maxY - this.minY) >> sectionExponent) + 1;
        final int sectionsZ = ((this.maxZ - this.minZ) >> sectionExponent) + 1;
        return (long) sectionsX * (long) sectionsY * (long) sectionsZ;
    }

    public static long sectionKey(final int x, final int y, final int z, final int sectionExponent) {
        final int sectionX = x >> sectionExponent;
        final int sectionY = y >> sectionExponent;
        final int sectionZ = z >> sectionExponent;
        return (long) sectionX << 40 | (long) sectionY << 20 | (long) sectionZ;
    }

    public void forEach(final int sectionExponent, final LongConsumer sectionConsumer) {
        final int minSectionX = this.minX >> sectionExponent;
        final int minSectionY = this.minY >> sectionExponent;
        final int minSectionZ = this.minZ >> sectionExponent;
        final int maxSectionX = this.maxX >> sectionExponent;
        final int maxSectionY = this.maxY >> sectionExponent;
        final int maxSectionZ = this.maxZ >> sectionExponent;

        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int y = minSectionY; y <= maxSectionY; y++) {
                for (int z = minSectionZ; z <= maxSectionZ; z++) {
                    sectionConsumer.accept(sectionKey(x, y, z, 0));
                }
            }
        }
    }
}
