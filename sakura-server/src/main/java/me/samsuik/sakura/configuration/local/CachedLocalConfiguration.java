package me.samsuik.sakura.configuration.local;

import io.papermc.paper.configuration.WorldConfiguration;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.explosion.durable.DurableMaterial;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import me.samsuik.sakura.redstone.RedstoneConfiguration;
import net.minecraft.world.level.block.Block;

import java.util.Map;

@SuppressWarnings("DataFlowIssue")
public final class CachedLocalConfiguration {
    public final long sectionKey;
    public final MinecraftMechanicsTarget mechanicsTarget;
    public final Map<Block, DurableMaterial> durableMaterials;
    public final RedstoneConfiguration redstoneBehaviour;
    public final boolean consistentExplosionRadius;
    public final int lavaFlowSpeed;
    public final MergeLevel mergeLevel;

    CachedLocalConfiguration(final long sectionKey, final ConfigurationContainer container) {
        this.sectionKey = sectionKey;
        this.mechanicsTarget = container.get(ConfigurationKeys.MECHANICS_TARGET);
        this.durableMaterials = ConfigurationHelpers.durableMaterials(container.get(ConfigurationKeys.DURABLE_MATERIALS));
        this.redstoneBehaviour = container.get(ConfigurationKeys.REDSTONE_BEHAVIOUR);
        this.consistentExplosionRadius = container.get(ConfigurationKeys.CONSISTENT_EXPLOSION_RADIUS);
        this.lavaFlowSpeed = container.get(ConfigurationKeys.LAVA_FLOW_SPEED);
        this.mergeLevel = container.get(ConfigurationKeys.MERGE_LEVEL);
    }

    public WorldConfiguration.Misc.RedstoneImplementation paperRedstoneImplementation() {
        return ConfigurationHelpers.toPaperImpl(this.redstoneBehaviour);
    }
}
