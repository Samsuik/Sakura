package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;

@NullMarked
public final class DataComponentHelper {
    public static int bucketMaxStackSize() {
        final GlobalConfiguration config = GlobalConfiguration.get();
        int stackSize = -1;
        if (config != null && config.players.bucketStackSize.isDefined()) {
            final int value = config.players.bucketStackSize.intValue();
            if (value > 0 && value <= Item.ABSOLUTE_MAX_STACK_SIZE) {
                stackSize = config.players.bucketStackSize.intValue();
            }
        }

        return stackSize;
    }

    public static boolean itemHasComponent(final ItemStack stack, final DataComponentType<?> component) {
        return stack.getComponentsPatch().get(stack, component) != null;
    }

    public static DataComponentMap modify(final DataComponentMap components, final Consumer<DataComponentMap.Builder> consumer) {
        final DataComponentMap.Builder builder = DataComponentMap.builder()
            .addAll(components);

        consumer.accept(builder);
        return builder.build();
    }
}
