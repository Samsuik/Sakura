package me.samsuik.sakura.player.visibility;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public final class PlayerVisibilitySettings implements VisibilitySettings {
    private static final String SETTINGS_COMPOUND_TAG = "clientVisibilitySettings";
    private final Reference2ObjectMap<VisibilityType, VisibilityState> visibilityStates = new Reference2ObjectOpenHashMap<>();

    @NonNull
    @Override
    public VisibilityState get(@NonNull VisibilityType type) {
        VisibilityState state = this.visibilityStates.get(type);
        return state != null ? state : type.getDefault();
    }

    @NonNull
    @Override
    public VisibilityState set(@NonNull VisibilityType type, @NonNull VisibilityState state) {
        if (type.isDefault(state)) {
            this.visibilityStates.remove(type);
        } else {
            this.visibilityStates.put(type, state);
        }
        return state;
    }

    @NonNull
    @Override
    public VisibilityState currentState() {
        int modifiedCount = this.visibilityStates.size();
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

    public void loadData(@NonNull ValueInput input) {
        input.child(SETTINGS_COMPOUND_TAG).ifPresent(settings -> {
            for (VisibilityType type : VisibilityTypes.types()) {
                String typeKey = type.key();
                String stateName = settings.getStringOr(typeKey, type.getDefault().name());
                this.set(type, VisibilityState.valueOf(stateName));
            }
        });
    }

    public void saveData(@NonNull ValueOutput output) {
        ValueOutput settings = output.child(SETTINGS_COMPOUND_TAG);
        this.visibilityStates.forEach((type, state) -> settings.putString(type.key(), state.name()));
    }
}
