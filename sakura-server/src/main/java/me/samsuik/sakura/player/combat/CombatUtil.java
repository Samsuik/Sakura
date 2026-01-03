package me.samsuik.sakura.player.combat;

import me.samsuik.sakura.player.item.BlockableSwordItem;
import me.samsuik.sakura.player.item.DataComponentHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jspecify.annotations.NullMarked;

import java.util.OptionalDouble;

@NullMarked
public final class CombatUtil {
    public static boolean overrideBlockingAndHalveDamage(final ItemStack stack, final LivingEntity entity) {
        return stack.getItem() instanceof BlockableSwordItem swordItem && swordItem.isSafeToOverrideBlocking(stack)
            || stack.is(Items.SHIELD) && !DataComponentHelper.itemHasComponent(stack, DataComponents.BLOCKS_ATTACKS)
                && entity.level().sakuraConfig().players.combat.shieldDamageReduction;
    }

    public static double getModifiedAttackDamage(final Level level, final ItemStack stack) {
        final double baseAttack = getItemAttackDamage(stack);
        double modifiedDamage = 0.0;

        if (baseAttack != 0.0 && level.sakuraConfig().players.combat.legacyCombatMechanics) {
            final OptionalDouble legacyAttack = LegacyDamageMapping.itemAttackDamage(stack.getItem());
            if (legacyAttack.isPresent()) {
                modifiedDamage = legacyAttack.getAsDouble() - baseAttack;
            }
        }

        final Double attackOverride = level.sakuraConfig().players.combat.itemAttackDamageOverride.get(stack.getItem());
        if (attackOverride != null) {
            modifiedDamage = attackOverride - baseAttack - 1;
        }

        return modifiedDamage;
    }

    public static double getItemAttackDamage(final ItemStack itemstack) {
        final ItemAttributeModifiers defaultModifiers = itemstack.getItem().components().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (defaultModifiers != null && !defaultModifiers.modifiers().isEmpty()) { // exists
            double baseAttack = 0.0;
            for (final ItemAttributeModifiers.Entry entry : defaultModifiers.modifiers()) {
                if (!entry.slot().test(EquipmentSlot.MAINHAND) || !entry.attribute().is(Attributes.ATTACK_DAMAGE))
                    continue;
                if (entry.modifier().operation() != AttributeModifier.Operation.ADD_VALUE)
                    return 0;
                baseAttack += entry.modifier().amount();
            }
            return baseAttack;
        }

        return 0.0;
    }

    public static float calculateLegacySharpnessDamage(final LivingEntity entity, final ItemStack itemstack, final DamageSource damageSource) {
        final Holder<Enchantment> enchantment = getEnchantmentHolder(Enchantments.SHARPNESS);
        final ItemEnchantments itemEnchantments = itemstack.getEnchantments();
        final int enchantmentLevel = itemEnchantments.getLevel(enchantment);
        final MutableFloat damage = new MutableFloat();

        if (entity.level() instanceof ServerLevel level) {
            enchantment.value().modifyDamage(level, enchantmentLevel, itemstack, entity, damageSource, damage);
        }
        // legacy - modern
        return enchantmentLevel * 1.25F - damage.getValue();
    }

    private static Holder<Enchantment> getEnchantmentHolder(final ResourceKey<Enchantment> enchantmentKey) {
        final RegistryAccess registryAccess = MinecraftServer.getServer().registryAccess();
        final HolderLookup.RegistryLookup<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        return enchantments.getOrThrow(enchantmentKey);
    }
}
