package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DataComponentHelper {
    public static int bucketMaxStackSize() {
        final GlobalConfiguration config = GlobalConfiguration.get();
        return config == null || !config.players.bucketStackSize.isDefined() ? -1 : config.players.bucketStackSize.intValue();
    }

    @SuppressWarnings("OptionalAssignedToNull")
    public static boolean itemHasComponent(final ItemStack stack, final DataComponentType<?> component) {
        return stack.getComponentsPatch().get(component) != null;
    }

    public static DataComponentMap copyComponentsAndModifyMaxStackSize(final DataComponentMap componentMap, final int maxItemSize) {
        if (maxItemSize > 0 && maxItemSize <= 99) {
            return DataComponentMap.builder()
                .addAll(componentMap)
                .set(DataComponents.MAX_STACK_SIZE, maxItemSize)
                .build();
        }
        return componentMap;
    }
}
