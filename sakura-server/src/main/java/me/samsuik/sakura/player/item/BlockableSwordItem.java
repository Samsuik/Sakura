package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlocksAttacks;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

@NullMarked
public final class BlockableSwordItem extends Item {
    private static final BlocksAttacks BLOCKS_ATTACKS = new BlocksAttacks(
        0.0f,
        0.0f,
        List.of(new BlocksAttacks.DamageReduction(90.0f, Optional.empty(), 0.0f, 0.5f)),
        BlocksAttacks.ItemDamageFunction.DEFAULT,
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

    public BlockableSwordItem(final Properties properties) {
        super(properties);
    }

    @Override
    public DataComponentMap modifyBaseComponents(final DataComponentMap components) {
        if (canBlockWithSwords(components)) {
            return DataComponentHelper.modify(components, builder -> builder.set(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS));
        }
        return components;
    }

    @Override
    public void modifyComponentsSentToClient(final PatchedDataComponentMap components) {
        if (canBlockWithSwords(components)) {
            components.sakura$patchComponent(DataComponents.BLOCKS_ATTACKS, BLOCKS_ATTACKS);
        }
    }

    private static boolean canBlockWithSwords(final DataComponentMap componentMap) {
        final GlobalConfiguration config = GlobalConfiguration.get();
        if (config == null || !config.players.combat.blockWithSwords) {
            return false;
        }

        final BlocksAttacks blocksAttacks = componentMap.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks != null && blocksAttacks != BLOCKS_ATTACKS) {
            return false;
        }

        return !componentMap.has(DataComponents.CONSUMABLE);
    }
}
