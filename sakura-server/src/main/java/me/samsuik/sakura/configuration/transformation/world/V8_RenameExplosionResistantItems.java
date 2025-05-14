package me.samsuik.sakura.configuration.transformation.world;

import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import static org.spongepowered.configurate.NodePath.path;

public final class V8_RenameExplosionResistantItems {
    private static final int VERSION = 8;
    private static final NodePath BASE_PATH = path("entity", "items");
    private static final NodePath OLD_WHITELIST_PATH = BASE_PATH.plus(path("use-whitelist-for-explosion-resistant-items"));
    private static final NodePath NEW_WHITELIST_PATH = BASE_PATH.plus(path("blast-resistant", "whitelist-over-blacklist"));
    private static final NodePath OLD_ITEMS_PATH = BASE_PATH.plus(path("explosion-resistant-items"));
    private static final NodePath NEW_ITEMS_PATH = BASE_PATH.plus(path("blast-resistant", "items"));

    public static void apply(ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(OLD_WHITELIST_PATH, move(NEW_WHITELIST_PATH))
            .addAction(OLD_ITEMS_PATH, move(NEW_ITEMS_PATH))
            .build());
    }

    private static TransformAction move(NodePath path) {
        return (p, n) -> path.array();
    }
}
