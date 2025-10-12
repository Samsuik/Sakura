package me.samsuik.sakura.mechanics;

import ca.spottedleaf.moonrise.patches.collisions.CollisionUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class EntityBehaviour {
    public static void changeEntityPosition(
        final Entity entity,
        final Vec3 position,
        final Vec3 relativeMovement,
        final MinecraftMechanicsTarget mechanicsTarget
    ) {
        final Vec3 newPosition = position.add(relativeMovement);
        final Vec3 newEntityPosition;
        if (mechanicsTarget.is(MechanicVersion.v1_21_5)) {
            newEntityPosition = manglePosition(position, relativeMovement);
            entity.addMovementThisTick(new Entity.Movement(position, newPosition, relativeMovement));
        } else {
            newEntityPosition = newPosition;
        }

        entity.setPos(newEntityPosition);
    }

    private static Vec3 manglePosition(final Vec3 position, final Vec3 relativeMovement) {
        Vec3 newPosition = position;
        for (final Direction.Axis axis : Direction.axisStepOrder(relativeMovement)) {
            final double movement = relativeMovement.get(axis);
            if (movement != 0.0) {
                newPosition = newPosition.relative(axis.getPositive(), movement);
            }
        }

        return newPosition;
    }

    public static boolean canMoveEntity(final double relativeMovementSqr, final Vec3 movement, final MinecraftMechanicsTarget mechanicsTarget) {
        return relativeMovementSqr > 1.0E-7
            || mechanicsTarget.atLeast(MechanicVersion.v1_21_2) && movement.lengthSqr() - relativeMovementSqr < 1.0E-7
            || mechanicsTarget.before(MechanicVersion.v1_14);
    }

    public static boolean canCheckInsideBlock(final BlockPos pos, final boolean canCollide, final LongSet visited, final MinecraftMechanicsTarget mechanicsTarget) {
        final boolean before1_21_9 = mechanicsTarget.before(MechanicVersion.v1_21_9);
        if (before1_21_9 && !visited.add(pos.asLong())) {
            return false;
        }
        return (mechanicsTarget.before(MechanicVersion.v1_21_2) || canCollide) && (before1_21_9 || visited.add(pos.asLong()));
    }

    public static boolean prioritiseXFirst(final double x, final double z, final @Nullable MinecraftMechanicsTarget mechanicsTarget) {
        return mechanicsTarget == null || mechanicsTarget.atLeast(MechanicVersion.v1_14)
            ? Math.abs(x) < Math.abs(z)
            : mechanicsTarget.isLegacy() && Math.abs(x) > Math.abs(z);
    }

    public static void convertVoxelsIntoAABBs(final Vec3 movement, final AABB bb, final List<VoxelShape> voxels, final List<AABB> aabbs) {
        final AABB collisions = bb.expandTowards(movement);
        for (final VoxelShape shape : voxels) {
            for (final AABB boundingBox : shape.toAabbs()) {
                if (CollisionUtil.voxelShapeIntersect(boundingBox, collisions) && !CollisionUtil.isEmpty(boundingBox)) {
                    aabbs.add(boundingBox);
                }
            }
        }

        voxels.clear();
    }
}
