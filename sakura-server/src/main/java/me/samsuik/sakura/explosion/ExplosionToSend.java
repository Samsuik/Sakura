package me.samsuik.sakura.explosion;

import net.minecraft.world.phys.Vec3;

public record ExplosionToSend(Vec3 position, int blocksDestroyed) {
}
