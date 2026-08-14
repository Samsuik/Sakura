package me.samsuik.sakura.configuration.local;

import io.papermc.paper.configuration.WorldConfiguration;
import me.samsuik.sakura.explosion.durable.DurableMaterial;
import me.samsuik.sakura.explosion.durable.DurableMaterialsContainer;
import me.samsuik.sakura.redstone.RedstoneConfiguration;
import me.samsuik.sakura.redstone.RedstoneImplementation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.bukkit.craftbukkit.block.CraftBlockType;

import java.util.HashMap;
import java.util.Map;

public final class ConfigurationHelpers {
    public static DurableMaterialsContainer.SealedDurableMaterialsContainer durableMaterialContainer(final Map<Block, DurableMaterial> materials) {
        final DurableMaterialsContainer container = new DurableMaterialsContainer();
        materials.forEach((block, material) -> container.set(CraftBlockType.minecraftToBukkitNew(block), material));
        return container.seal();
    }

    public static Map<Block, DurableMaterial> durableMaterials(final DurableMaterialsContainer container) {
        final Map<Block, DurableMaterial> durableMaterials = new HashMap<>();
        container.contents().forEach((type, material) -> durableMaterials.put(CraftBlockType.bukkitToMinecraftNew(type), material));
        return durableMaterials;
    }

    public static WorldConfiguration.Misc.RedstoneImplementation toPaperImpl(final RedstoneConfiguration redstoneConfig) {
        return WorldConfiguration.Misc.RedstoneImplementation.values()[redstoneConfig.implementation().ordinal()];
    }

    public static RedstoneConfiguration redstoneConfigurationDefault(final Level level) {
        final WorldConfiguration.Misc.RedstoneImplementation paperRedstoneImplementation = level.paperConfig().misc.redstoneImplementation;
        final RedstoneImplementation sakuraRedstoneImplementation = RedstoneImplementation.values()[paperRedstoneImplementation.ordinal()];
        return new RedstoneConfiguration(sakuraRedstoneImplementation, level.sakuraConfig().technical.redstone.redstoneCache);
    }
}
