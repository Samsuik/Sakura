package me.samsuik.sakura.configuration.serializer;

import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import me.samsuik.sakura.mechanics.MinecraftVersionEncoding;
import me.samsuik.sakura.mechanics.ServerType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NullMarked
public final class MinecraftMechanicsTargetSerializer implements TypeSerializer<MinecraftMechanicsTarget> {
    private static final NodePath MECHANIC_VERSION = NodePath.path("mechanic-version");
    private static final NodePath SERVER_TYPE = NodePath.path("server-type");

    @Override
    public MinecraftMechanicsTarget deserialize(final Type type, final ConfigurationNode root) throws SerializationException {
        final String mechanicVersion = root.node(MECHANIC_VERSION).getString();
        final String serverType = root.node(SERVER_TYPE).getString();

        return MinecraftMechanicsTarget.fromString("%s+%s".formatted(mechanicVersion, serverType));
    }

    @Override
    public void serialize(
        final Type type,
        @Nullable MinecraftMechanicsTarget mechanicsTarget,
        final ConfigurationNode root
    ) throws SerializationException {
        if (mechanicsTarget == null) {
            mechanicsTarget = MinecraftMechanicsTarget.latest();
        }

        root.node(MECHANIC_VERSION).set(MinecraftVersionEncoding.asString(mechanicsTarget.mechanicVersion()));
        root.node(SERVER_TYPE).set(ServerType.name(mechanicsTarget.serverType()));
    }
}
