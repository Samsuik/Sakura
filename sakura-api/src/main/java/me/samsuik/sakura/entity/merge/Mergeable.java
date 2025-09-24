package me.samsuik.sakura.entity.merge;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Mergeable {
    MergeLevel getMergeLevel();

    void setMergeLevel(final MergeLevel mergeLevel);

    int getStacked();

    void setStacked(int stacked);
}
