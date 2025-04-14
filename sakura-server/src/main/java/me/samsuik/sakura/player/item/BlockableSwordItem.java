package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BlockableSwordItem extends Item {
    private static final Consumable BLOCKING_ANIMATION = Consumable.builder()
        .consumeSeconds(3600)
        .animation(ItemUseAnimation.BLOCK)
        .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY))
        .hasConsumeParticles(false)
        .build();

    public BlockableSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void modifyComponentsSentToClient(PatchedDataComponentMap components) {
        if (hasCustomAnimationOrDisabled(components)) {
            // When updating to 1.22 change CONSUMABLE to BLOCK_ATTACKS
            components.set(DataComponents.CONSUMABLE, BLOCKING_ANIMATION);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (hasCustomAnimationOrDisabled(stack.getComponents())) {
            return super.use(level, player, hand);
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (hasCustomAnimationOrDisabled(stack.getComponents())) {
            return super.getUseDuration(stack, entity);
        } else {
            return BLOCKING_ANIMATION.consumeTicks();
        }
    }

    public boolean isSafeToOverrideBlocking(ItemStack stack) {
        return !hasCustomAnimationOrDisabled(stack.getComponents());
    }

    private static boolean hasCustomAnimationOrDisabled(DataComponentMap componentMap) {
        final GlobalConfiguration config = GlobalConfiguration.get();
        return (config == null || !config.players.combat.blockWithSwords)
            || componentMap.has(DataComponents.CONSUMABLE)
            || componentMap.has(DataComponents.BLOCKS_ATTACKS);
    }
}
