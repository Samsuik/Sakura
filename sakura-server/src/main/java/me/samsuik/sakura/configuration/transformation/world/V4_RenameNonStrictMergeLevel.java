package me.samsuik.sakura.configuration.transformation.world;

import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.util.Locale;

import static org.spongepowered.configurate.NodePath.path;

@NullMarked
public final class V4_RenameNonStrictMergeLevel implements TransformAction {
    private static final int VERSION = 4;
    private static final String OLD_LEVEL_NAME = "NON_STRICT";
    private static final String NEW_LEVEL_NAME = "LENIENT";
    private static final NodePath MERGE_LEVEL_PATH = path("cannons", "merge-level");
    private static final V4_RenameNonStrictMergeLevel INSTANCE = new V4_RenameNonStrictMergeLevel();

    private V4_RenameNonStrictMergeLevel() {}

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformations.transform(MERGE_LEVEL_PATH, INSTANCE));
    }

    @Override
    public Object @Nullable [] visitPath(final NodePath path, final ConfigurationNode value) throws ConfigurateException {
        final String level = value.getString();
        if (level != null && OLD_LEVEL_NAME.equals(level.toUpperCase(Locale.ENGLISH))) {
            value.set(NEW_LEVEL_NAME);
        }
        return null;
    }
}
