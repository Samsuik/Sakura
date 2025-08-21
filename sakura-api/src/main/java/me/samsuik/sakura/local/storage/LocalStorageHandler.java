package me.samsuik.sakura.local.storage;

import me.samsuik.sakura.local.LocalRegion;
import org.bukkit.Location;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Deprecated(forRemoval = true)
public interface LocalStorageHandler {
    default @NonNull Optional<LocalRegion> locate(@NonNull Location location) {
        return this.locate(location.blockX(), location.blockZ());
    }

    @NonNull Optional<LocalRegion> locate(int x, int z);

    @Nullable LocalValueStorage get(@NonNull LocalRegion region);

    boolean has(@NonNull LocalRegion region);

    void put(@NonNull LocalRegion region, @NonNull LocalValueStorage storage);

    void remove(@NonNull LocalRegion region);

    @NonNull List<LocalRegion> regions();
}
