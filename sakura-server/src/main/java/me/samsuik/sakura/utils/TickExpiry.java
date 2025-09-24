package me.samsuik.sakura.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class TickExpiry {
    private long tick;
    private final int expiration;

    public TickExpiry(final long tick, final int expiration) {
        Preconditions.checkArgument(expiration > 0, "Expiration cannot be lower or equal to 0");
        this.tick = tick;
        this.expiration = expiration;
    }

    public void refresh(final long tick) {
        this.tick = tick;
    }

    public boolean isExpired(final long tick) {
        return this.tick < tick - this.expiration;
    }
}
