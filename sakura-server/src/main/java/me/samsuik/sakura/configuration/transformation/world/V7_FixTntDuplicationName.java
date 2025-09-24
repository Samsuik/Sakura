package me.samsuik.sakura.configuration.transformation.world;

import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import static org.spongepowered.configurate.NodePath.*;
import static org.spongepowered.configurate.transformation.TransformAction.rename;

public final class V7_FixTntDuplicationName {
    private static final int VERSION = 7;
    private static final NodePath OLD_PATH = path("technical", "allow-t-n-t-duplication");
    private static final String NEW_NAME = "allow-tnt-duplication";

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformations.transform(OLD_PATH, rename(NEW_NAME)));
    }
}
