package me.samsuik.sakura.local;

import me.samsuik.sakura.physics.PhysicsVersion;
import me.samsuik.sakura.redstone.RedstoneImplementation;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Deprecated(forRemoval = true)
public final class LocalValueKeys {
    private static final String NAMESPACE = "sakura";

    public static final LocalValueKey<PhysicsVersion> PHYSICS_VERSION = create("physics-version", () -> PhysicsVersion.LATEST);
    public static final LocalValueKey<Map<Material, Map.Entry<Integer, Float>>> DURABLE_MATERIALS = create("durable-materials", HashMap::new);
    public static final LocalValueKey<RedstoneImplementation> REDSTONE_IMPLEMENTATION = create("redstone-implementation", () -> RedstoneImplementation.VANILLA);
    public static final LocalValueKey<Boolean> CONSISTENT_EXPLOSION_RADIUS = create("consistent-radius", () -> false);
    public static final LocalValueKey<Boolean> REDSTONE_CACHE = create("redstone-cache", () -> false);
    public static final LocalValueKey<Integer> LAVA_FLOW_SPEED = create("lava-flow-speed", () -> -1);

    private static <T> LocalValueKey<T> create(String key, Supplier<T> supplier) {
        return new LocalValueKey<>(new NamespacedKey(NAMESPACE, key), supplier);
    }
}
