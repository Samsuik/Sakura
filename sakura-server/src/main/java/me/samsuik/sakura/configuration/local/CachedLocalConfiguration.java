package me.samsuik.sakura.configuration.local;

import io.papermc.paper.configuration.WorldConfiguration;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.explosion.durable.DurableMaterial;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import me.samsuik.sakura.redstone.RedstoneConfiguration;
import me.samsuik.sakura.redstone.RedstoneImplementation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.stream.Collectors;

@NullMarked
public final class CachedLocalConfiguration {
    public final long sectionKey;
    public final MinecraftMechanicsTarget mechanicsTarget;
    public final Map<Block, DurableMaterial> durableMaterials;
    public final RedstoneConfiguration redstoneBehaviour;
    public final boolean consistentExplosionRadius;
    public final int lavaFlowSpeed;
    public final MergeLevel mergeLevel;

    public static CachedLocalConfiguration emptyConfiguration() {
        return new CachedLocalConfiguration();
    }

    public CachedLocalConfiguration(final Level level, final ConfigurationContainer container, final long sectionKey) {
        this.sectionKey = sectionKey;
        this.mechanicsTarget = container.getOptional(ConfigurationKeys.MECHANICS_TARGET)
            .orElse(level.sakuraConfig().cannons.mechanics.mechanicsTarget);
        this.durableMaterials = container.getOptional(ConfigurationKeys.DURABLE_MATERIALS)
            .map(sealedContainer -> sealedContainer.open().contents().entrySet().stream()
                .map(entry -> Map.entry(CraftBlockType.bukkitToMinecraftNew(entry.getKey()), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            .orElseGet(() -> level.sakuraConfig().cannons.explosion.durableMaterials);
        this.redstoneBehaviour = container.getOptional(ConfigurationKeys.REDSTONE_BEHAVIOUR)
            .orElse(createDefaultRedstoneConfiguration(level));
        this.consistentExplosionRadius = container.getOptional(ConfigurationKeys.CONSISTENT_EXPLOSION_RADIUS)
            .orElse(level.sakuraConfig().cannons.explosion.consistentRadius);
        this.lavaFlowSpeed = container.getOptional(ConfigurationKeys.LAVA_FLOW_SPEED)
            .orElse(30);
        this.mergeLevel = container.getOptional(ConfigurationKeys.MERGE_LEVEL)
            .orElse(level.sakuraConfig().cannons.mergeLevel);
    }

    private CachedLocalConfiguration() {
        this.sectionKey = Long.MIN_VALUE;
        this.mechanicsTarget = MinecraftMechanicsTarget.latest();
        this.durableMaterials = Map.of();
        this.redstoneBehaviour = new RedstoneConfiguration(RedstoneImplementation.VANILLA, false);
        this.consistentExplosionRadius = false;
        this.lavaFlowSpeed = 30;
        this.mergeLevel = MergeLevel.NONE;
    }

    public WorldConfiguration.Misc.RedstoneImplementation paperRedstoneImplementation() {
        return WorldConfiguration.Misc.RedstoneImplementation.values()[this.redstoneBehaviour.implementation().ordinal()];
    }

    private static RedstoneConfiguration createDefaultRedstoneConfiguration(final Level level) {
        final WorldConfiguration.Misc.RedstoneImplementation paperRedstoneImplementation = level.paperConfig().misc.redstoneImplementation;
        final RedstoneImplementation sakuraRedstoneImplementation = RedstoneImplementation.values()[paperRedstoneImplementation.ordinal()];
        return new RedstoneConfiguration(sakuraRedstoneImplementation, level.sakuraConfig().technical.redstone.redstoneCache);
    }
}
