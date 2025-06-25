package me.samsuik.sakura.configuration.transformation.world;

import io.papermc.paper.configuration.type.number.DoubleOr;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import static org.spongepowered.configurate.NodePath.path;

public final class V2_VerticalKnockbackUseDefault implements TransformAction {
    private static final int VERSION = 2;
    private static final NodePath PATH = path("players", "knockback", "knockback-vertical");
    private static final V2_VerticalKnockbackUseDefault INSTANCE = new V2_VerticalKnockbackUseDefault();

    private V2_VerticalKnockbackUseDefault() {}

    public static void apply(ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(PATH, INSTANCE)
            .build());
    }

    @Override
    public Object @Nullable [] visitPath(NodePath path, ConfigurationNode value) throws ConfigurateException {
        if (value.getDouble() == 0.4) {
            value.set(DoubleOr.Default.USE_DEFAULT);
        }
        return null;
    }
}
