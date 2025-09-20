package me.samsuik.sakura.configuration.local;

import io.papermc.paper.configuration.WorldConfiguration.Misc.RedstoneImplementation;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.samsuik.sakura.explosion.durable.DurableMaterial;
import me.samsuik.sakura.local.LocalValueKeys;
import me.samsuik.sakura.local.storage.LocalValueStorage;
import me.samsuik.sakura.physics.PhysicsVersion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import java.util.Map;

public final class LocalValueConfig {
    public Map<Block, DurableMaterial> durableMaterials;
    public RedstoneImplementation redstoneImplementation;
    public PhysicsVersion physicsVersion;
    public boolean consistentRadius;
    public boolean redstoneCache;
    public int lavaFlowSpeed = -1;

    LocalValueConfig(Level level) {
        this.durableMaterials = new Reference2ObjectOpenHashMap<>(level.sakuraConfig().cannons.explosion.durableMaterials);
        this.redstoneImplementation = level.paperConfig().misc.redstoneImplementation;
        this.physicsVersion = level.sakuraConfig().cannons.mechanics.physicsVersion;
        this.consistentRadius = level.sakuraConfig().cannons.explosion.consistentRadius;
        this.redstoneCache = level.sakuraConfig().technical.redstone.redstoneCache;
    }

    void loadFromStorage(LocalValueStorage storage) {
        storage.get(LocalValueKeys.DURABLE_MATERIALS).ifPresent(materials -> {
            materials.forEach((materialType, materialProperties) -> {
                Block nmsBlock = CraftMagicNumbers.getBlock(materialType);
                // temp, will be updated later
                DurableMaterial durableMaterial = new DurableMaterial(materialProperties.getKey(), materialProperties.getValue(), false);
                this.durableMaterials.put(nmsBlock, durableMaterial);
            });
        });
        storage.get(LocalValueKeys.REDSTONE_IMPLEMENTATION).ifPresent(implementation -> {
            this.redstoneImplementation = RedstoneImplementation.values()[implementation.ordinal()];
        });
        this.physicsVersion = storage.getOrDefault(LocalValueKeys.PHYSICS_VERSION, this.physicsVersion);
        this.consistentRadius = storage.getOrDefault(LocalValueKeys.CONSISTENT_EXPLOSION_RADIUS, this.consistentRadius);
        this.redstoneCache = storage.getOrDefault(LocalValueKeys.REDSTONE_CACHE, this.redstoneCache);
        this.lavaFlowSpeed = storage.getOrDefault(LocalValueKeys.LAVA_FLOW_SPEED, this.lavaFlowSpeed);
    }
}
