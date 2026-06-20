package me.samsuik.sakura.player.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StackableBucketItem extends BucketItem {
    public StackableBucketItem(final Fluid content, final Properties properties) {
        super(content, properties);
    }

    @Override
    public DataComponentMap modifyBaseComponents(final DataComponentMap components) {
        final int stackSize = this.customBucketStackSize();
        if (stackSize != -1) {
            return DataComponentHelper.modify(components, builder -> builder.set(DataComponents.MAX_STACK_SIZE, stackSize));
        }
        return components;
    }

    @Override
    public void modifyComponentsSentToClient(final PatchedDataComponentMap components) {
        final int stackSize = this.customBucketStackSize();
        if (stackSize != -1) {
            components.sakura$patchComponent(DataComponents.MAX_STACK_SIZE, stackSize);
        }
    }

    private int customBucketStackSize() {
        final int stackSize = DataComponentHelper.bucketMaxStackSize();
        if (stackSize != -1 && this.content.isSame(Fluids.EMPTY)) {
            return Math.max(stackSize, 16);
        }
        return stackSize;
    }
}
