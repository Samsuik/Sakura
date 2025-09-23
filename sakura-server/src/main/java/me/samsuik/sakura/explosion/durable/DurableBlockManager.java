package me.samsuik.sakura.explosion.durable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.TimeUnit;

@NullMarked
public final class DurableBlockManager {
    private final Cache<BlockPos, DurableBlock> durableBlocks = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .maximumSize(65534)
            .build();

    public boolean damage(BlockPos pos, DurableMaterial material) {
        DurableBlock block = this.durableBlocks.getIfPresent(pos);
        if (block == null) {
            this.durableBlocks.put(pos, block = new DurableBlock(material.durability()));
        }
        return block.damage();
    }

    public int durability(BlockPos pos, DurableMaterial material) {
        final DurableBlock block = this.durableBlocks.getIfPresent(pos);
        return block != null ? block.durability() : material.durability();
    }

    private static final class DurableBlock {
        private int durability;

        public DurableBlock(int durability) {
            this.durability = durability;
        }

        public int durability() {
            return this.durability;
        }

        public boolean damage() {
            return --this.durability <= 0;
        }
    }
}
