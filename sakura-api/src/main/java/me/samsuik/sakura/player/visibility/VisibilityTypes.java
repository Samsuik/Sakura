package me.samsuik.sakura.player.visibility;

import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public final class VisibilityTypes {
    private static final List<VisibilityType> TYPES = new ArrayList<>();

    public static final VisibilityType TNT = create("tnt", true);
    public static final VisibilityType WIND_CHARGE = create("wind_charge", true);
    public static final VisibilityType SAND = create("sand", true);
    public static final VisibilityType EXPLOSIONS = create("explosions", true);
    public static final VisibilityType SPAWNERS = create("spawners", false);
    public static final VisibilityType PISTONS = create("pistons", false);

    public static ImmutableList<VisibilityType> types() {
        return ImmutableList.copyOf(TYPES);
    }

    private static VisibilityType create(final String key, final boolean minimal) {
        final VisibilityType newVisibilityType = VisibilityType.from(key, minimal);
        TYPES.add(newVisibilityType);
        return newVisibilityType;
    }
}
