package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PowderedSnowBucketItem extends SolidBucketItem {
    public PowderedSnowBucketItem(final Properties properties) {
        super(Blocks.POWDER_SNOW, SoundEvents.BUCKET_EMPTY_POWDER_SNOW, properties);
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
        if (stackSize != -1 && stackablePowderedSnowBuckets()) {
            components.sakura$patchComponent(DataComponents.MAX_STACK_SIZE, stackSize);
        }
    }

    private static boolean stackablePowderedSnowBuckets() {
        final GlobalConfiguration config = GlobalConfiguration.get();
        return config != null && config.players.stackablePowderedSnowBuckets;
    }
}
