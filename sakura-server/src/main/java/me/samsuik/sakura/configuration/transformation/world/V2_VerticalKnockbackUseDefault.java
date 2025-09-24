package me.samsuik.sakura.configuration.transformation.world;

import io.papermc.paper.configuration.type.number.DoubleOr;
import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import static org.spongepowered.configurate.NodePath.path;

@NullMarked
public final class V2_VerticalKnockbackUseDefault implements TransformAction {
    private static final int VERSION = 2;
    private static final NodePath KNOCKBACK_VERTICAL_PATH = path("players", "knockback", "knockback-vertical");
    private static final V2_VerticalKnockbackUseDefault INSTANCE = new V2_VerticalKnockbackUseDefault();

    private V2_VerticalKnockbackUseDefault() {}

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformations.transform(KNOCKBACK_VERTICAL_PATH, INSTANCE));
    }

    @Override
    public Object @Nullable [] visitPath(final NodePath path, final ConfigurationNode value) throws ConfigurateException {
        if (value.getDouble() == 0.4) {
            value.set(DoubleOr.Default.USE_DEFAULT);
        }
        return null;
    }
}
