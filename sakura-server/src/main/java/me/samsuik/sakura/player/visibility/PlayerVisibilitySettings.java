package me.samsuik.sakura.player.visibility;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerVisibilitySettings implements VisibilitySettings {
    private static final String VISIBILITY_SETTINGS_TAG = "client_visibility_settings";
    private final Reference2ObjectMap<VisibilityType, VisibilityState> visibilityStates = new Reference2ObjectOpenHashMap<>();

    @Override
    public VisibilityState get(final VisibilityType type) {
        final VisibilityState state = this.visibilityStates.get(type);
        //noinspection ConstantValue
        return state != null ? state : type.getDefault();
    }

    @Override
    public VisibilityState set(final VisibilityType type, final VisibilityState state) {
        if (type.isDefault(state)) {
            this.visibilityStates.remove(type);
        } else {
            this.visibilityStates.put(type, state);
        }
        return state;
    }

    @Override
    public VisibilityState currentState() {
        final int modifiedCount = this.visibilityStates.size();
        if (modifiedCount == 0) {
            return VisibilityState.ON;
        } else if (modifiedCount != VisibilityTypes.types().size()) {
            return VisibilityState.MODIFIED;
        } else {
            return VisibilityState.OFF;
        }
    }

    @Override
    public boolean playerModified() {
        return !this.visibilityStates.isEmpty();
    }

    public void loadData(final ValueInput input) {
        input.child(VISIBILITY_SETTINGS_TAG).ifPresent(settings -> {
            for (final VisibilityType type : VisibilityTypes.types()) {
                final String typeKey = type.key();
                final String stateName = settings.getStringOr(typeKey, type.getDefault().name());
                this.set(type, VisibilityState.valueOf(stateName));
            }
        });
    }

    public void saveData(final ValueOutput output) {
        final ValueOutput settings = output.child(VISIBILITY_SETTINGS_TAG);
        this.visibilityStates.forEach((type, state) -> settings.putString(type.key(), state.name()));
    }
}
