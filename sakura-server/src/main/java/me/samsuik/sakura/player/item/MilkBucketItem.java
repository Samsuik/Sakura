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
    public DataComponentMap components() {
        final DataComponentMap components = super.components();
        return stackableMilkBuckets()
            ? DataComponentHelper.updateBucketMaxStackSize(components)
            : components;
    }

    @Override
    public void modifyComponentsSentToClient(final PatchedDataComponentMap components) {
        if (stackableMilkBuckets()) {
            components.sakura$patchComponent(DataComponents.MAX_STACK_SIZE, DataComponentHelper.bucketMaxStackSize());
        }
    }

    private static boolean stackableMilkBuckets() {
        final GlobalConfiguration config = GlobalConfiguration.get();
        return config != null && config.players.stackableMilkBuckets;
    }
}
