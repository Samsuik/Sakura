package me.samsuik.sakura.player.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StackableBucketItem extends BucketItem {
    public StackableBucketItem(final Fluid content, final Properties properties) {
        super(content, properties);
    }

    @Override
    public DataComponentMap components() {
        return DataComponentHelper.updateBucketMaxStackSize(super.components());
    }

    @Override
    public void modifyComponentsSentToClient(final PatchedDataComponentMap components) {
        final int maxStackSize = DataComponentHelper.bucketMaxStackSize();
        if (maxStackSize > 1 && maxStackSize <= 99) {
            components.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
        }
    }
}
