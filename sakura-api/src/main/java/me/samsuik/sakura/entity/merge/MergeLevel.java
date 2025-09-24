package me.samsuik.sakura.entity.merge;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum MergeLevel {
    /**
     * Disabled.
     */
    NONE(-1),
    /**
     * "STRICT" merges entities with the same properties, position, momentum and OOE.
     * This is considered safe to use, and will not break cannon mechanics.
     */
    STRICT(1),
    /**
     * "LENIENT" merges entities aggressively by tracking the entities that have
     * previously merged. This is a hybrid of "SPAWN" and "STRICT" merging, with the
     * visuals of "STRICT" merging and better merging potential of "SPAWN" merging.
     */
    LENIENT(2),
    /**
     * "SPAWN" merges entities one gametick after they have spawned. Merging is
     * only possible after it has been established that the entity is safe to
     * merge by collecting information on the entities that merge together over time.
     */
    SPAWN(3);

    private final int level;

    MergeLevel(final int level) {
        this.level = level;
    }

    public final boolean atLeast(MergeLevel level) {
        return this.getLevel() >= level.getLevel();
    }

    public final int getLevel() {
        return this.level;
    }
}
