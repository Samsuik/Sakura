package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.AttackRange;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SpearItem extends Item {
    private static final AttackRange REDUCED_ATTACK_RANGE = new AttackRange(0.0f, 3.0f, 0.0f, 5.0f, 0.0f, 1.0f);

    public SpearItem(final Properties properties) {
        super(properties);
    }

    @Override
    public DataComponentMap modifyBaseComponents(final DataComponentMap components) {
        final GlobalConfiguration config = GlobalConfiguration.get();
        if (config != null && !config.players.combat.spearsHaveExtraAttackRange) {
            return DataComponentHelper.modify(components, builder -> builder.set(DataComponents.ATTACK_RANGE, REDUCED_ATTACK_RANGE));
        }

        return components;
    }

    @Override
    public void modifyComponentsSentToClient(final PatchedDataComponentMap components) {
        final GlobalConfiguration config = GlobalConfiguration.get();
        if (config != null && !config.players.combat.spearsHaveExtraAttackRange) {
            components.sakura$patchComponent(DataComponents.ATTACK_RANGE, REDUCED_ATTACK_RANGE);
        }
    }
}
