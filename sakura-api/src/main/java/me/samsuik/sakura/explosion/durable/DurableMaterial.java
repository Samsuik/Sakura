package me.samsuik.sakura.explosion.durable;

import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@NullMarked
@ConfigSerializable
public record DurableMaterial(int durability, float resistance, boolean onlyDamagedByTnt) {
    public DurableMaterial(final int durability, final float resistance) {
        this(durability, resistance, true);
    }

    public boolean replaceBlastResistance() {
        return this.resistance >= 0.0f;
    }

    public boolean applyDurability() {
        return this.durability >= 0;
    }

    public static DurableMaterial durability(final int durability) {
        return new DurableMaterial(durability, 0.0f);
    }

    public static DurableMaterial resistance(final float resistance) {
        return new DurableMaterial(1, resistance);
    }

    public static DurableMaterial likeSand(final int durability) {
        return new DurableMaterial(durability, 3.0f);
    }

    public static DurableMaterial likeCobblestone(final int durability) {
        return new DurableMaterial(durability, 6.0f);
    }

    public static DurableMaterial likeEndstone(final int durability) {
        return new DurableMaterial(durability, 9.0f);
    }
}
