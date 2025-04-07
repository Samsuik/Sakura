package me.samsuik.sakura.configuration.local;

import me.samsuik.sakura.local.LocalRegion;

public final class OverlappingRegionException extends RuntimeException {
    public OverlappingRegionException(LocalRegion presentRegion, LocalRegion region) {
        super("overlapping region (%s, %s)".formatted(presentRegion, region));
    }
}
