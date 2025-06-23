package me.samsuik.sakura.local;

import io.papermc.paper.math.Position;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record LocalRegion(int minX, int minZ, int maxX, int maxZ) {
    public static LocalRegion from(BoundingBox boundingBox) {
        return of(boundingBox.getMin(), boundingBox.getMax());
    }

    public static LocalRegion of(Vector min, Vector max) {
        return of(min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());
    }

    public static LocalRegion of(Position min, Position max) {
        return of(min.blockX(), min.blockZ(), max.blockX(), max.blockZ());
    }

    public static LocalRegion of(int minX, int minZ, int maxX, int maxZ) {
        return new LocalRegion(
            Math.min(minX, maxX), Math.min(minZ, maxZ),
            Math.max(minX, maxX), Math.max(minZ, maxZ)
        );
    }

    public static LocalRegion at(int x, int z, int radius) {
        return new LocalRegion(x-radius, z-radius, x+radius, z+radius);
    }

    public boolean intersects(LocalRegion region) {
        return (this.minX <= region.minX() && this.maxX >= region.minX() || this.maxX >= region.maxX() && this.minX < region.maxX())
            && (this.minZ <= region.minZ() && this.maxZ >= region.minZ() || this.maxZ >= region.maxZ() && this.minZ < region.maxZ());
    }

    public boolean contains(LocalRegion region) {
        return this.minX < region.minX() && this.maxX > region.maxX()
            && this.maxZ < region.minZ() && this.maxZ > region.maxZ();
    }

    public boolean contains(int x, int z) {
        return this.minX <= x && this.maxX >= x && this.minZ <= z && this.maxZ >= z;
    }
}
