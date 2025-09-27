package me.samsuik.sakura.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NullMarked;

/**
 * A replica of the explosion raytrace code before it was replaced in Minecraft 1.14.
 */
@NullMarked
public final class LegacyExplosionBlockClipping {
    private static final double EPSILON = 1.0e-7f; // the precision loss is intentional

    private Vec3 currentPos;
    private final Vec3 endPos;
    private final int endX;
    private final int endY;
    private final int endZ;

    private LegacyExplosionBlockClipping(final Vec3 currentPos, final Vec3 endPos) {
        this.currentPos = currentPos;
        this.endPos = endPos;
        this.endX = Mth.floor(endPos.x);
        this.endY = Mth.floor(endPos.y);
        this.endZ = Mth.floor(endPos.z);
    }

    public static BlockHitResult.Type clip(final Level level, final Vec3 startPos, final Vec3 endPos) {
        final LegacyExplosionBlockClipping clipDetection = new LegacyExplosionBlockClipping(startPos, endPos);
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(startPos.x(), startPos.y(), startPos.z());
        LevelChunk chunk = null;
        int steps = 0;

        do {
            final int chunkX = mutableBlockPos.getX() >> 4;
            final int chunkZ = mutableBlockPos.getZ() >> 4;
            if (chunk == null || chunkX != chunk.locX || chunkZ != chunk.locZ) {
                chunk = level.getChunkIfLoaded(chunkX, chunkZ);
                if (chunk == null) break;
            }

            final BlockState state = chunk.getBlockState(mutableBlockPos);
            final VoxelShape shape = state.getShape(level, mutableBlockPos);
            for (final AABB shapeBB : shape.toAabbs()) {
                if (clip(shapeBB, mutableBlockPos, clipDetection.currentPos, endPos)) {
                    return HitResult.Type.BLOCK;
                }
            }
        } while (++steps < 16 && clipDetection.next(mutableBlockPos));

        return HitResult.Type.MISS;
    }

    private boolean next(final BlockPos.MutableBlockPos mutableBlockPos) {
        final int currX = mutableBlockPos.getX();
        final int currY = mutableBlockPos.getY();
        final int currZ = mutableBlockPos.getZ();
        final int toX = this.endX;
        final int toY = this.endY;
        final int toZ = this.endZ;

        if (currX == toX && currY == toY && currZ == toZ) {
            return false;
        }

        boolean moveX = true;
        boolean moveY = true;
        boolean moveZ = true;
        double d0 = 999.0D;
        double d1 = 999.0D;
        double d2 = 999.0D;

        if (toX > currX) {
            d0 = (double) currX + 1.0D;
        } else if (toX < currX) {
            d0 = (double) currX + 0.0D;
        } else {
            moveX = false;
        }

        if (toY > currY) {
            d1 = (double) currY + 1.0D;
        } else if (toY < currY) {
            d1 = (double) currY + 0.0D;
        } else {
            moveY = false;
        }

        if (toZ > currZ) {
            d2 = (double) currZ + 1.0D;
        } else if (toZ < currZ) {
            d2 = (double) currZ + 0.0D;
        } else {
            moveZ = false;
        }

        double d3 = 999.0D;
        double d4 = 999.0D;
        double d5 = 999.0D;

        final Vec3 currPos = this.currentPos;
        final Vec3 endPos = this.endPos;
        final double d6 = endPos.x - currPos.x;
        final double d7 = endPos.y - currPos.y;
        final double d8 = endPos.z - currPos.z;

        if (moveX) d3 = (d0 - currPos.x) / d6;
        if (moveY) d4 = (d1 - currPos.y) / d7;
        if (moveZ) d5 = (d2 - currPos.z) / d8;

        if (d3 == -0.0D) d3 = -1.0E-4D;
        if (d4 == -0.0D) d4 = -1.0E-4D;
        if (d5 == -0.0D) d5 = -1.0E-4D;

        final Direction moveDir;
        final Vec3 newCurrentPos;
        if (d3 < d4 && d3 < d5) {
            moveDir = toX > currX ? Direction.WEST : Direction.EAST;
            newCurrentPos = new Vec3(d0, currPos.y + d7 * d3, currPos.z + d8 * d3);
        } else if (d4 < d5) {
            moveDir = toY > currY ? Direction.DOWN : Direction.UP;
            newCurrentPos = new Vec3(currPos.x + d6 * d4, d1, currPos.z + d8 * d4);
        } else {
            moveDir = toZ > currZ ? Direction.NORTH : Direction.SOUTH;
            newCurrentPos = new Vec3(currPos.x + d6 * d5, currPos.y + d7 * d5, d2);
        }

        mutableBlockPos.set(
            Mth.floor(newCurrentPos.x) - (moveDir == Direction.EAST ? 1 : 0),
            Mth.floor(newCurrentPos.y) - (moveDir == Direction.UP ? 1 : 0),
            Mth.floor(newCurrentPos.z) - (moveDir == Direction.SOUTH ? 1 : 0)
        );

        this.currentPos = newCurrentPos;
        return true;
    }

    private static boolean clip(final AABB bb, final BlockPos pos, final Vec3 from, final Vec3 to) {
        final Vec3 origin    = from.subtract(pos.getX(), pos.getY(), pos.getZ());
        final Vec3 direction = to.subtract(pos.getX(), pos.getY(), pos.getZ()).subtract(origin);
        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        if (direction.x * direction.x >= EPSILON) {
            final double t1 = (bb.minX - origin.x) / direction.x;
            final double t2 = (bb.maxX - origin.x) / direction.x;
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        } else if (origin.x < bb.minX || origin.x > bb.maxX) {
            return false;
        }

        if (direction.y * direction.y >= EPSILON) {
            final double t1 = (bb.minY - origin.y) / direction.y;
            final double t2 = (bb.maxY - origin.y) / direction.y;
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        } else if (origin.y < bb.minY || origin.y > bb.maxY) {
            return false;
        }

        if (direction.z * direction.z >= EPSILON) {
            double t1 = (bb.minZ - origin.z) / direction.z;
            double t2 = (bb.maxZ - origin.z) / direction.z;
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        } else if (origin.z < bb.minZ || origin.z > bb.maxZ) {
            return false;
        }

        return tmax >= tmin && tmax >= 0.0 && tmin <= 1.0;
    }
}
