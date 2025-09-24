package me.samsuik.sakura.configuration.transformation.global;

import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.util.Map;

import static org.spongepowered.configurate.NodePath.path;

@NullMarked
public final class V1_RelocateMessages {
    private static final int VERSION = 2; // targeted version is always ahead by one
    private static final Map<NodePath, NodePath> RELOCATION = Map.of(
        path("fps", "message"), path("messages", "fps-setting-change"),
        path("players", "potato-message"), path("messages", "durable-block-interaction")
    );

    private V1_RelocateMessages() {}

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        final ConfigurationTransformation.Builder transformationBuilder = ConfigurationTransformation.builder();
        for (final Map.Entry<NodePath, NodePath> entry : RELOCATION.entrySet()) {
            transformationBuilder.addAction(entry.getKey(), relocate(entry.getValue()));
        }
        builder.addVersion(VERSION, transformationBuilder.build());
    }

    private static TransformAction relocate(final NodePath path) {
        return (node, object) -> path.array();
    }
}
