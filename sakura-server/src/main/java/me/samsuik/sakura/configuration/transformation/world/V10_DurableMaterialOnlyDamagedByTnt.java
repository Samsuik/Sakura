package me.samsuik.sakura.configuration.transformation.world;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import static org.spongepowered.configurate.NodePath.path;

@NullMarked
public final class V10_DurableMaterialOnlyDamagedByTnt implements TransformAction {
    private static final int VERSION = 10;
    private static final V10_DurableMaterialOnlyDamagedByTnt INSTANCE = new V10_DurableMaterialOnlyDamagedByTnt();
    private static final NodePath EXPLOSION_PATH = path("cannons", "explosion");
    private static final NodePath DURABLE_MATERIALS_PATH = EXPLOSION_PATH.plus(path("durable-materials"));
    private static final NodePath REQUIRE_TNT_PATH = EXPLOSION_PATH.plus(path("require-tnt-to-damage-durable-materials"));

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(DURABLE_MATERIALS_PATH, INSTANCE)
            .addAction(REQUIRE_TNT_PATH, TransformAction.remove())
            .build());
    }

    @Override
    public Object @Nullable [] visitPath(final NodePath path, final ConfigurationNode value) throws ConfigurateException {
        for (final ConfigurationNode child : value.childrenList()) {
            child.node(path("only-damaged-by-tnt"), true);
        }

        return new Object[0];
    }
}
