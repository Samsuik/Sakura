package me.samsuik.sakura.configuration.transformation.world;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import static org.spongepowered.configurate.NodePath.path;

public final class V6_FixIncorrectExtraKnockback implements TransformAction {
    private static final int VERSION = 6;
    private static final NodePath PATH = path("players", "knockback", "sprinting", "extra-knockback");
    private static final V6_FixIncorrectExtraKnockback INSTANCE = new V6_FixIncorrectExtraKnockback();

    private V6_FixIncorrectExtraKnockback() {}

    public static void apply(ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(PATH, INSTANCE)
            .build());
    }

    @Override
    public Object @Nullable [] visitPath(NodePath path, ConfigurationNode value) throws ConfigurateException {
        if (value.getDouble() == 1.0) {
            value.set(0.5);
        }
        return null;
    }
}
