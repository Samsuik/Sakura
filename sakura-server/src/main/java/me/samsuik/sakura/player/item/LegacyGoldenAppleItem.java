package me.samsuik.sakura.player.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

@NullMarked
@SuppressWarnings("OptionalAssignedToNull")
public final class LegacyGoldenAppleItem extends Item {
    private static final Consumable LEGACY_ENCHANTED_GOLDEN_APPLE = Consumables.defaultFood()
        .onConsume(
            new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.REGENERATION, 600, 4),
                    new MobEffectInstance(MobEffects.RESISTANCE, 6000, 0),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0),
                    new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0)
                )
            )
        )
        .build();

    public LegacyGoldenAppleItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.itemHasConsumableComponent(stack, level)) {
            return super.use(level, player, hand);
        } else {
            return LEGACY_ENCHANTED_GOLDEN_APPLE.startConsuming(player, stack, hand);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (this.itemHasConsumableComponent(stack, level)) {
            return super.finishUsingItem(stack, level, entity);
        } else {
            return LEGACY_ENCHANTED_GOLDEN_APPLE.onConsume(level, entity, stack);
        }
    }

    private boolean itemHasConsumableComponent(ItemStack stack, Level level) {
        Optional<?> consumable = stack.getComponentsPatch().get(DataComponents.CONSUMABLE);
        return consumable != null || !level.sakuraConfig().players.combat.oldEnchantedGoldenApple;
    }
}
