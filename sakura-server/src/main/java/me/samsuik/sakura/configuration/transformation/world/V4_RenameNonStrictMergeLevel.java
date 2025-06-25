package me.samsuik.sakura.configuration.transformation.world;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.util.Locale;

import static org.spongepowered.configurate.NodePath.path;

public final class V4_RenameNonStrictMergeLevel implements TransformAction {
    private static final int VERSION = 4;
    private static final String OLD_LEVEL_NAME = "NON_STRICT";
    private static final String NEW_LEVEL_NAME = "LENIENT";
    private static final NodePath PATH = path("cannons", "merge-level");
    private static final V4_RenameNonStrictMergeLevel INSTANCE = new V4_RenameNonStrictMergeLevel();

    private V4_RenameNonStrictMergeLevel() {}

    public static void apply(ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(PATH, INSTANCE)
            .build());
    }

    @Override
    public Object @Nullable [] visitPath(NodePath path, ConfigurationNode value) throws ConfigurateException {
        String level = value.getString();
        if (level != null && OLD_LEVEL_NAME.equals(level.toUpperCase(Locale.ENGLISH))) {
            value.set(NEW_LEVEL_NAME);
        }
        return null;
    }
}
