package me.samsuik.sakura.configuration.local;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.bukkit.util.BoundingBox;

import java.util.Comparator;

public record ConfigurationBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public static final ConfigurationBounds EMPTY = new ConfigurationBounds(
        Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
        Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
    );

    public static ConfigurationBounds fromBukkitBoundingBox(final BoundingBox boundingBox) {
        return new ConfigurationBounds(
            Mth.floor(boundingBox.getMinX()),
            Mth.floor(boundingBox.getMinY()),
            Mth.floor(boundingBox.getMinZ()),
            Mth.floor(boundingBox.getMaxX()),
            Mth.floor(boundingBox.getMaxY()),
            Mth.floor(boundingBox.getMaxZ())
        );
    }

    public BoundingBox asBukkitBoundingBox() {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public int sizeX() {
        return this.maxX - this.minX;
    }

    public int sizeY() {
        return this.maxY - this.minY;
    }

    public int sizeZ() {
        return this.maxZ - this.minZ;
    }

    public int largestXZAxis() {
        return Math.max(this.sizeX(), this.sizeZ());
    }

    public Vec3i center() {
        return new Vec3i(
            (this.minX + this.maxX) / 2,
            (this.minY + this.maxY) / 2,
            (this.minZ + this.maxZ) / 2
        );
    }

    public boolean contains(final int x, final int y, final int z) {
        return x >= this.minX && x <= this.maxX
            && y >= this.minY && y <= this.maxY
            && z >= this.minZ && z <= this.maxZ;
    }

    public boolean contains(final ConfigurationBounds bounds) {
        return bounds.minX >= this.minX && bounds.maxX <= this.maxX
            && bounds.minY >= this.minY && bounds.maxY <= this.maxY
            && bounds.minZ >= this.minZ && bounds.maxZ <= this.maxZ;
    }

    public boolean intersects(final ConfigurationBounds bounds) {
        return bounds.minX <= this.maxX && bounds.maxX >= this.minX
            && bounds.minY <= this.maxY && bounds.maxY >= this.minY
            && bounds.minZ <= this.maxZ && bounds.maxZ >= this.minZ;
    }

    public ConfigurationBounds expand(final ConfigurationBounds bounds) {
        return new ConfigurationBounds(
            Math.min(this.minX, bounds.minX),
            Math.min(this.minY, bounds.minY),
            Math.min(this.minZ, bounds.minZ),
            Math.max(this.maxX, bounds.maxX),
            Math.max(this.maxY, bounds.maxY),
            Math.max(this.maxZ, bounds.maxZ)
        );
    }

    public long volume() {
        final long x = (this.maxX - this.minX) + 1;
        final long y = (this.maxY - this.minY) + 1;
        final long z = (this.maxZ - this.minZ) + 1;
        return x * y * z;
    }
}
