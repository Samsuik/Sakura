package me.samsuik.sakura.configuration.local;

import me.samsuik.sakura.entity.TntSpread;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import me.samsuik.sakura.redstone.RedstoneConfiguration;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jspecify.annotations.NullMarked;

import static me.samsuik.sakura.explosion.durable.DurableMaterialsContainer.*;

@NullMarked
public final class ConfigurationKeys {
    public static final ConfigurationKey<MinecraftMechanicsTarget> MECHANICS_TARGET = create("mechanics_target", MinecraftMechanicsTarget.class);
    public static final ConfigurationKey<SealedDurableMaterialsContainer> DURABLE_MATERIALS = create("durable_materials", SealedDurableMaterialsContainer.class);
    public static final ConfigurationKey<RedstoneConfiguration> REDSTONE_BEHAVIOUR = create("redstone_behaviour", RedstoneConfiguration.class);
    public static final ConfigurationKey<Boolean> CONSISTENT_EXPLOSION_RADIUS = create("consistent_explosion_radius", Boolean.class);
    public static final ConfigurationKey<Integer> LAVA_FLOW_SPEED = create("lava_flow_speed", Integer.class);
    public static final ConfigurationKey<MergeLevel> MERGE_LEVEL = create("merge_level", MergeLevel.class);
    public static final ConfigurationKey<TntSpread> TNT_SPREAD = create("tnt_spread", TntSpread.class);
    public static final ConfigurationKey<Boolean> TNT_FLOWS_IN_WATER = create("tnt_flows_in_water", Boolean.class);
    public static final ConfigurationKey<Boolean> HEIGHT_PARITY = create("height_parity", Boolean.class);
    public static final ConfigurationKey<Boolean> FLOATING_POINT_FIX = create("floating_point_fix", Boolean.class);
    public static final ConfigurationKey<Boolean> BROKEN_PAPER_EXPLOSION_BEHAVIOUR = create("broken_paper_explosion_behaviour", Boolean.class);
    public static final ConfigurationKey<Integer> HEIGHT_LIMIT = create("height_limit", Integer.class);

    private static <T> ConfigurationKey<T> create(final @KeyPattern.Value String name, final Class<T> clazz) {
        return new ConfigurationKey<>(Key.key("sakura", name), clazz);
    }
}
