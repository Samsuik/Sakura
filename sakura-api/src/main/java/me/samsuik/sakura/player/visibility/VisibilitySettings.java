package me.samsuik.sakura.player.visibility;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface VisibilitySettings {
    default boolean isEnabled(final VisibilityType type) {
        return this.get(type) == VisibilityState.ON;
    }

    default boolean isDisabled(final VisibilityType type) {
        return this.get(type) == VisibilityState.OFF;
    }

    default boolean isToggled(final VisibilityType type) {
        return !type.isDefault(this.get(type));
    }

    default VisibilityState toggle(final VisibilityType type) {
        final VisibilityState state = this.get(type);
        return this.set(type, toggleState(state));
    }

    default VisibilityState cycle(final VisibilityType type) {
        final VisibilityState state = this.get(type);
        return this.set(type, type.cycle(state));
    }

    default void toggleAll() {
        final VisibilityState state = this.currentState();
        final VisibilityState newState = toggleState(state);
        for (final VisibilityType type : VisibilityTypes.types()) {
            this.set(type, newState);
        }
    }

    VisibilityState get(final VisibilityType type);

    VisibilityState set(final VisibilityType type, final VisibilityState state);

    VisibilityState currentState();

    boolean playerModified();

    static VisibilityState toggleState(final VisibilityState state) {
        return state != VisibilityState.OFF
            ? VisibilityState.OFF
            : VisibilityState.ON;
    }
}
