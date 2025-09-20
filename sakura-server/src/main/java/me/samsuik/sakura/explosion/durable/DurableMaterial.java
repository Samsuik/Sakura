package me.samsuik.sakura.explosion.durable;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;

@ConfigSerializable
public record DurableMaterial(int durability, float resistance, @Required boolean onlyDamagedByTnt) {
}
