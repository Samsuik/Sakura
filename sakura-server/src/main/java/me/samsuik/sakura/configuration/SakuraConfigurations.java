package me.samsuik.sakura.configuration;

import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.configuration.*;
import io.papermc.paper.configuration.mapping.InnerClassFieldDiscoverer;
import io.papermc.paper.configuration.serializer.*;
import io.papermc.paper.configuration.serializer.collection.map.FastutilMapSerializer;
import io.papermc.paper.configuration.serializer.collection.TableSerializer;
import io.papermc.paper.configuration.serializer.registry.RegistryHolderSerializer;
import io.papermc.paper.configuration.serializer.registry.RegistryValueSerializer;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import me.samsuik.sakura.configuration.serializer.MinecraftMechanicsTargetSerializer;
import me.samsuik.sakura.configuration.transformation.ConfigurationTransformations;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.FieldDiscoverer;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static io.leangen.geantyref.GenericTypeReflector.erase;
import static io.papermc.paper.configuration.PaperConfigurations.defaultFieldProcessors;

@NullMarked
@SuppressWarnings("Convert2Diamond")
public final class SakuraConfigurations extends Configurations<GlobalConfiguration, WorldConfiguration> {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    static final String GLOBAL_CONFIG_FILE_NAME = "sakura-global.yml";
    static final String WORLD_DEFAULTS_CONFIG_FILE_NAME = "sakura-world-defaults.yml";
    static final String WORLD_CONFIG_FILE_NAME = "sakura-world.yml";
    public static final String CONFIG_DIR = "config";

    private static final String GLOBAL_HEADER = String.format("""
            This is the global configuration file for Sakura.
            As you can see, there's a lot to configure. Some options may impact gameplay, so use
            with caution, and make sure you know what each option does before configuring.

            The world configuration options have been moved inside
            their respective world folder. The files are named %s""", WORLD_CONFIG_FILE_NAME);

    private static final String WORLD_DEFAULTS_HEADER = """
            This is the world defaults configuration file for Sakura.
            As you can see, there's a lot to configure. Some options may impact gameplay, so use
            with caution, and make sure you know what each option does before configuring.

            Configuration options here apply to all worlds, unless you specify overrides inside
            the world-specific config file inside each world folder.""";

    private static final Function<ContextMap, String> WORLD_HEADER = map -> String.format("""
        This is a world configuration file for Sakura.
        This file may start empty but can be filled with settings to override ones in the %s/%s
        
        World: %s (%s)""",
        SakuraConfigurations.CONFIG_DIR,
        SakuraConfigurations.WORLD_DEFAULTS_CONFIG_FILE_NAME,
        map.require(WORLD_NAME),
        map.require(WORLD_KEY)
    );

    public SakuraConfigurations(final Path globalFolder) {
        super(globalFolder, GlobalConfiguration.class, WorldConfiguration.class, GLOBAL_CONFIG_FILE_NAME, WORLD_DEFAULTS_CONFIG_FILE_NAME, WORLD_CONFIG_FILE_NAME);
    }

    @Override
    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return super.createLoaderBuilder()
            .defaultOptions(PaperConfigurations::defaultOptions);
    }

    @Override
    protected ObjectMapper.Factory.Builder createGlobalObjectMapperFactoryBuilder() {
        return defaultGlobalFactoryBuilder(super.createGlobalObjectMapperFactoryBuilder());
    }

    private static ObjectMapper.Factory.Builder defaultGlobalFactoryBuilder(ObjectMapper.Factory.Builder builder) {
        return builder.addDiscoverer(InnerClassFieldDiscoverer.globalConfig(defaultFieldProcessors()));
    }

    @Override
    protected YamlConfigurationLoader.Builder createGlobalLoaderBuilder(RegistryAccess registryAccess) {
        return super.createGlobalLoaderBuilder(registryAccess)
            .defaultOptions(SakuraConfigurations::defaultGlobalOptions);
    }

    private static ConfigurationOptions defaultGlobalOptions(ConfigurationOptions options) {
        return options.header(GLOBAL_HEADER);
    }

    @Override
    public GlobalConfiguration initializeGlobalConfiguration(final RegistryAccess registryAccess) throws ConfigurateException {
        GlobalConfiguration configuration = super.initializeGlobalConfiguration(registryAccess);
        GlobalConfiguration.set(configuration);
        return configuration;
    }

    @Override
    protected ObjectMapper.Factory.Builder createWorldObjectMapperFactoryBuilder(final ContextMap contextMap) {
        return super.createWorldObjectMapperFactoryBuilder(contextMap)
            .addNodeResolver(new NestedSetting.Factory())
            .addDiscoverer(createWorldConfigFieldDiscoverer(contextMap));
    }

    private static FieldDiscoverer<?> createWorldConfigFieldDiscoverer(final ContextMap contextMap) {
        final Map<Class<?>, Object> overrides = Map.of(
            WorldConfiguration.class, createWorldConfigInstance(contextMap)
        );
        return InnerClassFieldDiscoverer.create(overrides, defaultFieldProcessors());
    }

    private static WorldConfiguration createWorldConfigInstance(ContextMap contextMap) {
        return new WorldConfiguration(contextMap.require(Configurations.WORLD_KEY));
    }

    @Override
    protected YamlConfigurationLoader.Builder createWorldConfigLoaderBuilder(final ContextMap contextMap) {
        final RegistryAccess access = contextMap.require(REGISTRY_ACCESS);
        return super.createWorldConfigLoaderBuilder(contextMap)
            .defaultOptions(options -> options
                .header(contextMap.require(WORLD_NAME).equals(WORLD_DEFAULTS) ? WORLD_DEFAULTS_HEADER : WORLD_HEADER.apply(contextMap))
                .serializers(serializers -> serializers
                    .register(new TypeToken<MinecraftMechanicsTarget>() {}, new MinecraftMechanicsTargetSerializer())
                    .register(new TypeToken<Reference2IntMap<?>>() {}, new FastutilMapSerializer.SomethingToPrimitive<Reference2IntMap<?>>(Reference2IntOpenHashMap::new, Integer.TYPE))
                    .register(new TypeToken<Reference2LongMap<?>>() {}, new FastutilMapSerializer.SomethingToPrimitive<Reference2LongMap<?>>(Reference2LongOpenHashMap::new, Long.TYPE))
                    .register(new TypeToken<Table<?, ?, ?>>() {}, new TableSerializer())
                    .register(StringRepresentableSerializer::isValidFor, new StringRepresentableSerializer())
                    .register(new RegistryValueSerializer<>(new TypeToken<EntityType<?>>() {}, access, Registries.ENTITY_TYPE, true))
                    .register(new RegistryValueSerializer<>(Item.class, access, Registries.ITEM, true))
                    .register(new RegistryValueSerializer<>(Block.class, access, Registries.BLOCK, true))
                    .register(new RegistryHolderSerializer<>(new TypeToken<ConfiguredFeature<?, ?>>() {}, access, Registries.CONFIGURED_FEATURE, false))
                )
            );
    }

    @Override
    protected void applyWorldConfigTransformations(final ContextMap contextMap, final ConfigurationNode node, final @Nullable ConfigurationNode defaultsNode) throws ConfigurateException {
        ConfigurationTransformations.worldTransformations(node);
    }

    @Override
    protected void applyGlobalConfigTransformations(final ConfigurationNode node) throws ConfigurateException {
        ConfigurationTransformations.globalTransformations(node);
    }

    @Override
    public WorldConfiguration createWorldConfig(final ContextMap contextMap) {
        final String levelName = contextMap.require(WORLD_NAME);
        try {
            return super.createWorldConfig(contextMap);
        } catch (IOException exception) {
            throw new RuntimeException("Could not create world config for " + levelName, exception);
        }
    }

    @Override
    protected boolean isConfigType(final Type type) {
        return ConfigurationPart.class.isAssignableFrom(erase(type));
    }

    @Override
    protected int globalConfigVersion() {
        return GlobalConfiguration.CURRENT_VERSION;
    }

    @Override
    protected int worldConfigVersion() {
        return WorldConfiguration.CURRENT_VERSION;
    }

    public void reloadConfigs(MinecraftServer server) {
        try {
            this.initializeGlobalConfiguration(server.registryAccess(), reloader(this.globalConfigClass, GlobalConfiguration.get()));
            this.initializeWorldDefaultsConfiguration(server.registryAccess());
            for (ServerLevel level : server.getAllLevels()) {
                this.createWorldConfig(createWorldContextMap(level), reloader(this.worldConfigClass, level.sakuraConfig()));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not reload sakura configuration files", ex);
        }
    }

    private static ContextMap createWorldContextMap(ServerLevel level) {
        return createWorldContextMap(level.levelStorageAccess.levelDirectory.path(), level.serverLevelData.getLevelName(), level.dimension().location(), level.registryAccess());
    }

    public static ContextMap createWorldContextMap(Path dir, String levelName, ResourceLocation worldKey, RegistryAccess registryAccess) {
        return ContextMap.builder()
            .put(WORLD_DIRECTORY, dir)
            .put(WORLD_NAME, levelName)
            .put(WORLD_KEY, worldKey)
            .put(REGISTRY_ACCESS, registryAccess)
            .build();
    }

    public static SakuraConfigurations setup(final Path configDir) {
        try {
            PaperConfigurations.createDirectoriesSymlinkAware(configDir);
            return new SakuraConfigurations(configDir);
        } catch (final IOException ex) {
            throw new RuntimeException("Could not setup PaperConfigurations", ex);
        }
    }

}
