package me.samsuik.sakura.configuration.transformation.world;

import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import static me.samsuik.sakura.configuration.transformation.ConfigurationTransformations.move;
import static org.spongepowered.configurate.NodePath.path;

@NullMarked
public final class V8_RenameExplosionResistantItems {
    private static final int VERSION = 8;
    private static final NodePath ITEMS_PATH = path("entity", "items");
    private static final NodePath OLD_WHITELIST_PATH = ITEMS_PATH.plus(path("use-whitelist-for-explosion-resistant-items"));
    private static final NodePath NEW_WHITELIST_PATH = ITEMS_PATH.plus(path("blast-resistant", "whitelist-over-blacklist"));
    private static final NodePath OLD_ITEMS_PATH = ITEMS_PATH.plus(path("explosion-resistant-items"));
    private static final NodePath NEW_ITEMS_PATH = ITEMS_PATH.plus(path("blast-resistant", "items"));

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        final ConfigurationTransformation transform = ConfigurationTransformation.builder()
            .addAction(OLD_WHITELIST_PATH, move(NEW_WHITELIST_PATH))
            .addAction(OLD_ITEMS_PATH, move(NEW_ITEMS_PATH))
            .build();
        builder.addVersion(VERSION, transform);
    }
}
