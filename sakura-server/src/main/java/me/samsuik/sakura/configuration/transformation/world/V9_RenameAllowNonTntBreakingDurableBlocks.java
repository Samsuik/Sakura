package me.samsuik.sakura.configuration.transformation.world;

import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import static me.samsuik.sakura.configuration.transformation.ConfigurationTransformations.move;
import static me.samsuik.sakura.configuration.transformation.ConfigurationTransformations.newValue;
import static org.spongepowered.configurate.NodePath.*;

@NullMarked
public final class V9_RenameAllowNonTntBreakingDurableBlocks {
    private static final int VERSION = 9;
    private static final NodePath EXPLOSION_PATH = path("cannons", "explosion");
    private static final NodePath OLD_PATH = EXPLOSION_PATH.plus(path("allow-non-tnt-breaking-durable-blocks"));
    private static final NodePath NEW_PATH = EXPLOSION_PATH.plus(path("require-tnt-to-damage-durable-materials"));

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        final ConfigurationTransformation transform = ConfigurationTransformation.builder()
            .addAction(OLD_PATH, move(NEW_PATH))
            .addAction(NEW_PATH, newValue(v -> !v.getBoolean()))
            .build();
        builder.addVersion(VERSION, transform);
    }
}
