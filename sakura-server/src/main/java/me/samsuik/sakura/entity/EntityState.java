package me.samsuik.sakura.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public record EntityState(
    Vec3 oldPosition,
    Vec3 position,
    Vec3 momentum,
    AABB bb,
    Vec3 stuckSpeed,
    Optional<BlockPos> supportingPos,
    boolean onGround,
    double fallDistance
) {
    public static EntityState of(final Entity entity) {
        return new EntityState(
            entity.oldPosition(),
            entity.position(),
            entity.getDeltaMovement(),
            entity.getBoundingBox(),
            entity.stuckSpeedMultiplier,
            entity.mainSupportingBlockPos,
            entity.onGround(),
            entity.fallDistance
        );
    }

    public void apply(final Entity entity) {
        entity.setOldPosAndRot(this.oldPosition, 0.0f, 0.0f);
        entity.setPos(this.position);
        entity.setDeltaMovement(this.momentum);
        entity.setBoundingBox(this.bb);
        entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), this.stuckSpeed);
        entity.onGround = this.onGround;
        entity.mainSupportingBlockPos = this.supportingPos;
        entity.fallDistance = this.fallDistance;
    }

    public boolean comparePositionAndMotion(Entity entity) {
        return entity.position().equals(this.position)
            && entity.getDeltaMovement().equals(this.momentum);
    }
}
