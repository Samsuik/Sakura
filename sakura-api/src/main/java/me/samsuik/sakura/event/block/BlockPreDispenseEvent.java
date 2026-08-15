package me.samsuik.sakura.event.block;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BlockPreDispenseEvent extends BlockEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private int delay;

    public BlockPreDispenseEvent(final Block block, final int delay) {
        super(block);
        this.delay = delay;
    }

    public int getDelay() {
        return this.delay;
    }

    public void setDelay(final @Range(from = 1, to = 1200) int delay) {
        this.delay = delay;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
