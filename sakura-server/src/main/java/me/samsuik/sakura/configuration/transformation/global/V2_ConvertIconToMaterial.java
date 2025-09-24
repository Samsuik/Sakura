package me.samsuik.sakura.configuration.transformation.global;

import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

@NullMarked
public final class V2_ConvertIconToMaterial implements TransformAction {
    private static final int VERSION = 3; // targeted version is always ahead by one
    private static final NodePath FPS_MATERIAL_PATH = NodePath.path("fps", "material");
    private static final V2_ConvertIconToMaterial INSTANCE = new V2_ConvertIconToMaterial();

    private V2_ConvertIconToMaterial() {}

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformations.transform(FPS_MATERIAL_PATH, INSTANCE));
    }

    @Override
    public Object @Nullable [] visitPath(final NodePath path, final ConfigurationNode value) throws ConfigurateException {
        if (value.raw() instanceof String stringValue) {
            value.raw(stringValue.toUpperCase());
        }
        return null;
    }
}
