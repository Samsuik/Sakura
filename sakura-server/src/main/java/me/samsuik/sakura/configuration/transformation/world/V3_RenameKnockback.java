package me.samsuik.sakura.configuration.transformation.world;

import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.util.Map;

import static org.spongepowered.configurate.NodePath.path;
import static org.spongepowered.configurate.transformation.TransformAction.*;

@NullMarked
public final class V3_RenameKnockback {
    private static final int VERSION = 3;
    private static final Map<NodePath, String> RENAME = Map.of(
        path("players", "knockback", "vertical-limit-require-ground"), "vertical-knockback-require-ground",
        path("players", "knockback", "knockback-horizontal"), "base-knockback"
    );

    private V3_RenameKnockback() {}

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        final ConfigurationTransformation.Builder transformationBuilder = ConfigurationTransformation.builder();
        for (final Map.Entry<NodePath, String> entry : RENAME.entrySet()) {
            transformationBuilder.addAction(entry.getKey(), rename(entry.getValue()));
        }
        builder.addVersion(VERSION, transformationBuilder.build());
    }
}
