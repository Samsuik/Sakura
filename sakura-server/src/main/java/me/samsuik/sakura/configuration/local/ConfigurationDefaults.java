package me.samsuik.sakura.configuration.local;

import net.minecraft.world.level.Level;

public final class ConfigurationDefaults {
    public static ConfigurationContainer.SealedConfigurationContainer levelDefaults(final Level level) {
        final ConfigurationContainer container = new ConfigurationContainer();
        container.set(ConfigurationKeys.MECHANICS_TARGET, level.sakuraConfig().cannons.mechanics.mechanicsTarget);
        container.set(ConfigurationKeys.DURABLE_MATERIALS, ConfigurationHelpers.durableMaterialContainer(level.sakuraConfig().cannons.explosion.durableMaterials));
        container.set(ConfigurationKeys.REDSTONE_BEHAVIOUR, ConfigurationHelpers.redstoneConfigurationDefault(level));
        container.set(ConfigurationKeys.CONSISTENT_EXPLOSION_RADIUS, level.sakuraConfig().cannons.explosion.consistentRadius);
        container.set(ConfigurationKeys.LAVA_FLOW_SPEED, -1);
        container.set(ConfigurationKeys.MERGE_LEVEL, level.sakuraConfig().cannons.mergeLevel);
        return container.seal();
    }
}
