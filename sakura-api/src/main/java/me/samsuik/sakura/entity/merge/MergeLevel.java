package me.samsuik.sakura.entity.merge;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum MergeLevel {
    NONE,
    STRICT,
    LENIENT,
    SPAWN
}
