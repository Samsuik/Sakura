package me.samsuik.sakura.player.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StackableBucketItem extends BucketItem {
    public StackableBucketItem(final Fluid content, final Properties properties) {
        super(content, properties);
    }

    @Override
    public void verifyComponentsAfterLoad(final ItemStack stack) {
        // It's also possible to override the components method and modify the stack size through the DataComponentHelper
        final int maxStackSize = DataComponentHelper.bucketMaxStackSize();
        if (maxStackSize > 0 && maxStackSize < 100) {
            stack.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
        }
    }
}
