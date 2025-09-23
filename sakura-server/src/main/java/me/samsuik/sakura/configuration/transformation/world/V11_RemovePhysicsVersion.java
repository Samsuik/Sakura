package me.samsuik.sakura.configuration.transformation.world;

import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import static org.spongepowered.configurate.NodePath.path;

public final class V11_RemovePhysicsVersion {
    private static final int VERSION = 11;
    private static final NodePath PATH = path("cannons", "mechanics", "physics-version");

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(PATH, TransformAction.remove())
            .build());
    }
}
