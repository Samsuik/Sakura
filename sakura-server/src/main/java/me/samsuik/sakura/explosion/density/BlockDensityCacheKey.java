package me.samsuik.sakura.explosion.density;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record BlockDensityCacheKey(Vec3 explosionPos, Vec3 entityPos) {
    public BlockDensityCacheKey(final Vec3 explosionPos, final Entity entity) {
        this(explosionPos, entity.position());
    }

    public static int getLenientKey(final Vec3 explosionPos, final BlockPos entityBlockPos) {
        int key        = Mth.floor(explosionPos.x());
        key = 31 * key + Mth.floor(explosionPos.y());
        key = 31 * key + Mth.floor(explosionPos.z());
        key = 31 * key + entityBlockPos.getX();
        key = 31 * key + entityBlockPos.getY();
        key = 31 * key + entityBlockPos.getZ();
        return key;
    }
}
