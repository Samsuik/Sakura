package me.samsuik.sakura.configuration.transformation.world;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.util.List;

import static org.spongepowered.configurate.NodePath.path;

public final class V5_CombineLoadChunksOptions implements TransformAction {
    private static final int VERSION = 5;
    private static final List<String> ENTITY_PATHS = List.of("tnt", "sand");
    private static final String OLD_NAME = "loads-chunks";
    private static final String NAME = "load-chunks";
    private static final NodePath PATH = path("cannons");
    private static final V5_CombineLoadChunksOptions INSTANCE = new V5_CombineLoadChunksOptions();

    private V5_CombineLoadChunksOptions() {}

    public static void apply(ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(PATH, INSTANCE)
            .build());
    }

    @Override
    public Object @Nullable [] visitPath(NodePath path, ConfigurationNode value) throws ConfigurateException {
        boolean shouldLoadChunks = false;

        for (String entity : ENTITY_PATHS) {
            NodePath entityPath = NodePath.path(entity, OLD_NAME);
            if (value.hasChild(entityPath)) {
                ConfigurationNode node = value.node(entityPath);
                shouldLoadChunks |= node.getBoolean();
                node.raw(null);
            }
        }

        value.node(NAME).set(shouldLoadChunks);
        return null;
    }
}
