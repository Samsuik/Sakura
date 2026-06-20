package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MilkBucketItem extends Item {
    public MilkBucketItem(final Properties properties) {
        super(properties);
    }

    @Override
    public DataComponentMap modifyBaseComponents(final DataComponentMap components) {
        final int stackSize = DataComponentHelper.bucketMaxStackSize();
        if (stackSize != -1) {
            return DataComponentHelper.modify(components, builder -> builder.set(DataComponents.MAX_STACK_SIZE, stackSize));
        }
        return components;
    }

    @Override
    public void modifyComponentsSentToClient(final PatchedDataComponentMap components) {
        final int stackSize = DataComponentHelper.bucketMaxStackSize();
        if (stackSize != -1 && stackableMilkBuckets()) {
            components.sakura$patchComponent(DataComponents.MAX_STACK_SIZE, stackSize);
        }
    }

    private static boolean stackableMilkBuckets() {
        final GlobalConfiguration config = GlobalConfiguration.get();
        return config != null && config.players.stackableMilkBuckets;
    }
}
