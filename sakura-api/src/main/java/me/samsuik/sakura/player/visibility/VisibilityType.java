package me.samsuik.sakura.player.visibility;

import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record VisibilityType(String key, ImmutableList<VisibilityState> states) {
    public VisibilityState getDefault() {
        return this.states.getFirst();
    }

    public boolean isDefault(final VisibilityState state) {
        return state == this.getDefault();
    }

    public VisibilityState cycle(final VisibilityState state) {
        final int index = this.states.indexOf(state);
        final int next = (index + 1) % this.states.size();
        return this.states.get(next);
    }

    public static VisibilityType from(final String key, final boolean minimal) {
        return new VisibilityType(key, states(minimal));
    }

    private static ImmutableList<VisibilityState> states(final boolean minimal) {
        final ImmutableList.Builder<VisibilityState> states = ImmutableList.builder();
        states.add(VisibilityState.ON);
        if (minimal) {
            states.add(VisibilityState.MINIMAL);
        }
        states.add(VisibilityState.OFF);
        return states.build();
    }
}
