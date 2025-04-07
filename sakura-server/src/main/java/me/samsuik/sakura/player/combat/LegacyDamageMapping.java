package me.samsuik.sakura.player.combat;

import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.OptionalDouble;

public final class LegacyDamageMapping {
    private static final Reference2DoubleMap<Item> LEGACY_ITEM_DAMAGE_MAP = new Reference2DoubleOpenHashMap<>();

    public static OptionalDouble itemAttackDamage(Item item) {
        double result = LEGACY_ITEM_DAMAGE_MAP.getDouble(item);
        return result == Double.MIN_VALUE ? OptionalDouble.empty() : OptionalDouble.of(result);
    }

    private interface ItemDamageRemapper {
        double apply(Item item, double attackDamage);
    }

    static {
        LEGACY_ITEM_DAMAGE_MAP.defaultReturnValue(Double.MIN_VALUE);

        // tool material is no longer exposed
        LEGACY_ITEM_DAMAGE_MAP.put(Items.WOODEN_AXE, 3.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.GOLDEN_AXE, 3.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.STONE_AXE, 4.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.IRON_AXE, 5.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.DIAMOND_AXE, 6.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.NETHERITE_AXE, 7.0);

        Reference2ObjectMap<TagKey<Item>, ItemDamageRemapper> remapUsingItemTags = new Reference2ObjectArrayMap<>();
        remapUsingItemTags.put(ItemTags.SWORDS, (item, attack) -> 1.0);
        remapUsingItemTags.put(ItemTags.PICKAXES, (item, attack) -> 1.0);
        remapUsingItemTags.put(ItemTags.SHOVELS, (item, attack) -> -0.5);
        remapUsingItemTags.put(ItemTags.HOES, (item, attack) -> -attack);

        for (Item item : BuiltInRegistries.ITEM) {
            ItemAttributeModifiers modifiers = item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);

            if (modifiers == null || LEGACY_ITEM_DAMAGE_MAP.containsKey(item)) {
                continue;
            }

            Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
            assert itemHolder.is(ItemTags.AXES) : "missing axe mapping";

            double attackDamage = modifiers.modifiers().stream()
                .filter(e -> e.attribute().is(Attributes.ATTACK_DAMAGE))
                .mapToDouble(e -> e.modifier().amount())
                .sum();

            if (attackDamage > 0.0) {
                double adjustment = 0.0;
                for (TagKey<Item> key : remapUsingItemTags.keySet()) {
                    if (itemHolder.is(key)) {
                        ItemDamageRemapper remapper = remapUsingItemTags.get(key);
                        adjustment = remapper.apply(item, attackDamage);
                    }
                }

                LEGACY_ITEM_DAMAGE_MAP.put(item, attackDamage + adjustment);
            }
        }
    }

    private LegacyDamageMapping() {}
}
