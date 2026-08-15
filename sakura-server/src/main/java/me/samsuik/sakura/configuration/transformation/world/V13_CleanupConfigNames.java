package me.samsuik.sakura.configuration.transformation.world;

import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.samsuik.sakura.configuration.transformation.ConfigurationTransformations.*;
import static org.spongepowered.configurate.NodePath.path;
import static org.spongepowered.configurate.transformation.TransformAction.remove;
import static org.spongepowered.configurate.transformation.TransformAction.rename;

public final class V13_CleanupConfigNames {
    private static final Map<NodePath, String> RENAMES = Map.of(
        path("cannons", "mechanics", "falling-block-parity"), "height-parity",
        path("cannons", "mechanics", "falling-block-floating-point-fix"), "floating-point-fix",
        path("cannons", "restrictions", "instant-block-fall-limit"), "instant-block-falling-limit"
    );

    private static final NodePath PREVENT_STACKING = path("cannons", "sand", "prevent-stacking");

    private static final NodePath OLD_WORLD_HEIGHT = PREVENT_STACKING.plus(path("world-height"));
    private static final NodePath NEW_WORLD_HEIGHT = path("cannons", "sand", "can-stack-at-world-height");
    private static final NodePath OLD_AGAINST_BORDER = PREVENT_STACKING.plus(path("against-border"));
    private static final NodePath NEW_AGAINST_BORDER = path("cannons", "sand", "can-stack-against-border");

    private static final NodePath OLD_COLLIDABLE_BLOCKS = path("cannons", "treat-collidable-blocks-as-full");
    private static final NodePath NEW_COLLIDABLE_BLOCKS = path("cannons", "collide-with-non-full-blocks");

    private static final List<NodePath> CONFUSING_WHITELIST_PATHS = List.of(
        path("entity", "items", "blast-resistant", "whitelist-over-blacklist"),
        path("entity", "items", "explosion-item-drops", "whitelist-over-blacklist")
    );
    private static final String NEW_WHITELIST_NAME = "whitelist";

    private static final int VERSION = 13;

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        final List<ConfigurationTransformation> transformations = new ArrayList<>();
        RENAMES.forEach((nodePath, name) -> transformations.add(ConfigurationTransformations.transform(nodePath, rename(name))));

        transformations.add(ConfigurationTransformations.transform(OLD_WORLD_HEIGHT, move(NEW_WORLD_HEIGHT)));
        transformations.add(ConfigurationTransformations.transform(OLD_AGAINST_BORDER, move(NEW_AGAINST_BORDER)));

        transformations.add(ConfigurationTransformations.transform(NEW_WORLD_HEIGHT, flip()));
        transformations.add(ConfigurationTransformations.transform(NEW_AGAINST_BORDER, flip()));

        transformations.add(ConfigurationTransformations.transform(OLD_COLLIDABLE_BLOCKS.plus(path("while-moving")), move(NEW_COLLIDABLE_BLOCKS)));
        transformations.add(ConfigurationTransformations.transform(OLD_COLLIDABLE_BLOCKS, remove()));
        transformations.add(ConfigurationTransformations.transform(PREVENT_STACKING, remove()));

        for (final NodePath path : CONFUSING_WHITELIST_PATHS) {
            transformations.add(ConfigurationTransformations.transform(path, rename(NEW_WHITELIST_NAME)));
        }

        builder.addVersion(VERSION, transformations.toArray(new ConfigurationTransformation[0]));
    }
}
