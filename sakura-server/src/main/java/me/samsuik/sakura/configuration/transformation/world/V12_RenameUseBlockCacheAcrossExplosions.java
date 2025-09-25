package me.samsuik.sakura.configuration.transformation.world;

import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import static org.spongepowered.configurate.transformation.TransformAction.rename;

public final class V12_RenameUseBlockCacheAcrossExplosions {
    private static final int VERSION = 12;
    private static final NodePath USE_BLOCK_CACHE_ACROSS_EXPLOSIONS_PATH = NodePath.path("cannons", "explosion", "use-block-cache-across-explosions");
    private static final String NEW_NAME = "reuse-block-cache-across-explosions";

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformations.transform(USE_BLOCK_CACHE_ACROSS_EXPLOSIONS_PATH, rename(NEW_NAME)));
    }
}
