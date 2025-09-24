package me.samsuik.sakura.configuration.transformation.world;

import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import static org.spongepowered.configurate.NodePath.path;

@NullMarked
public final class V11_RemovePhysicsVersion {
    private static final int VERSION = 11;
    private static final NodePath PHYSICS_VERSION_PATH = path("cannons", "mechanics", "physics-version");

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformations.transform(PHYSICS_VERSION_PATH, TransformAction.remove()));
    }
}
