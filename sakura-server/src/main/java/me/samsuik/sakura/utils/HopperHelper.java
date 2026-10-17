package me.samsuik.sakura.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.DefaultDrop;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class HopperHelper {
    public static void insertItemsOnDeath(final ServerLevel level, final Entity entity, final List<@Nullable DefaultDrop> drops) {
        final BlockEntity blockEntity = level.getBlockEntity(entity.getBlockPosBelowThatAffectsMyMovement());
        if (!(blockEntity instanceof final net.minecraft.world.level.block.entity.HopperBlockEntity hopper)) {
            return;
        }

        for (int index = 0; index < drops.size(); index++) {
            final DefaultDrop drop = drops.get(index);
            if (drop == null) {
                continue;
            }

            final ItemStack stack = drop.stack();
            //noinspection ConstantValue
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) {
                continue;
            }

            final ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), CraftItemStack.asNMSCopy(stack));
            if (HopperBlockEntity.addItem(hopper, itemEntity)) {
                drops.remove(index--);
            } else {
                drops.set(index, new DefaultDrop(itemEntity.getItem(), drop.dropConsumer()));
            }
        }
    }
}
