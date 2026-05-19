package me.samsuik.sakura.configuration.local;

import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import me.samsuik.sakura.redstone.RedstoneConfiguration;
import org.jspecify.annotations.NullMarked;

import static me.samsuik.sakura.explosion.durable.DurableMaterialsContainer.*;

@NullMarked
public final class ConfigurationKeys {
    public static final ConfigurationKey<MinecraftMechanicsTarget> MECHANICS_TARGET = new ConfigurationKey<>(MinecraftMechanicsTarget.class);
    public static final ConfigurationKey<SealedDurableMaterialsContainer> DURABLE_MATERIALS = new ConfigurationKey<>(SealedDurableMaterialsContainer.class);
    public static final ConfigurationKey<RedstoneConfiguration> REDSTONE_BEHAVIOUR = new ConfigurationKey<>(RedstoneConfiguration.class);
    public static final ConfigurationKey<Boolean> CONSISTENT_EXPLOSION_RADIUS = new ConfigurationKey<>(Boolean.class);
    public static final ConfigurationKey<Integer> LAVA_FLOW_SPEED = new ConfigurationKey<>(Integer.class);
    public static final ConfigurationKey<MergeLevel> MERGE_LEVEL = new ConfigurationKey<>(MergeLevel.class);
}
