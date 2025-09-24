package me.samsuik.sakura.mechanics;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
            entity.addMovementThisTick(new Entity.Movement(position, newPosition, true));
        } else {
            newEntityPosition = newPosition;
        }

        entity.setPos(newEntityPosition);
    }

    private static Vec3 manglePosition(final Vec3 position, final Vec3 relativeMovement) {
        Vec3 newPosition = position;
        for (final Direction.Axis axis : Entity.axisStepOrder(relativeMovement)) {
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

    public static boolean prioritiseXFirst(final double x, final double z, final @Nullable MinecraftMechanicsTarget mechanicsTarget) {
        return mechanicsTarget == null || mechanicsTarget.atLeast(MechanicVersion.v1_14)
            ? Math.abs(x) < Math.abs(z)
            : mechanicsTarget.isLegacy() && Math.abs(x) > Math.abs(z);
    }
}
