package me.samsuik.sakura.configuration;

import io.papermc.paper.configuration.Configuration;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.type.number.IntOr;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@SuppressWarnings({"CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "NotNullFieldNotInitialized", "InnerClassMayBeStatic", "RedundantSuppression"})
public final class GlobalConfiguration extends ConfigurationPart {
    static final int CURRENT_VERSION = 3;

    private static GlobalConfiguration instance;
    public static GlobalConfiguration get() {
        return instance;
    }

    static void set(final GlobalConfiguration instance) {
        GlobalConfiguration.instance = instance;
    }

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Messages messages;
    public final class Messages extends ConfigurationPart {
        public String durableBlockInteraction = "<dark_gray>(<light_purple>S</light_purple>) <white>This block has <gray><remaining></gray> of <gray><durability>";
        public String fpsSettingChange = "<dark_gray>(<light_purple>S</light_purple>) <gray><state> <yellow><name>";
        public String mechanicInformation = "<dark_gray>(<light_purple>S</light_purple>) <white>To view mechanic information: <dark_gray><information><<yellow>hover here</yellow>></information>";
        public boolean tpsShowEntityAndChunkCount = true;

        public Component fpsSettingChangeComponent(final String name, final String state) {
            return MiniMessage.miniMessage().deserialize(
                this.fpsSettingChange,
                Placeholder.unparsed("name", name),
                Placeholder.unparsed("state", state)
            );
        }

        public Component durableBlockInteractionComponent(final int remaining, final int durability) {
            return MiniMessage.miniMessage().deserialize(
                this.durableBlockInteraction,
                Placeholder.unparsed("remaining", String.valueOf(remaining)),
                Placeholder.unparsed("durability", String.valueOf(durability))
            );
        }

        public Component mechanicInformationComponent(final Component hoverComponent) {
            return MiniMessage.miniMessage().deserialize(
                this.mechanicInformation,
                TagResolver.resolver("information", Tag.styling(HoverEvent.showText(hoverComponent)))
            );
        }
    }

    public Fps fps;
    public final class Fps extends ConfigurationPart {
        public Material material = Material.PINK_STAINED_GLASS_PANE;
    }

    public Players players;
    public final class Players extends ConfigurationPart {
        public Combat combat = new Combat();
        public final class Combat extends ConfigurationPart {
            public boolean blockWithSwords = false;
        }

        public IntOr.Default bucketStackSize = IntOr.Default.USE_DEFAULT;
        public boolean stackableMilkBuckets = false;
        public boolean stackablePowderedSnowBuckets = false;
    }

    public Environment environment;
    public final class Environment extends ConfigurationPart {
        @Comment("This is only intended for plot worlds. Will affect chunk generation on servers.")
        public boolean calculateBiomeNoiseOncePerChunkSection = false;

        public MobSpawnerDefaults mobSpawnerDefaults = new MobSpawnerDefaults();
        public final class MobSpawnerDefaults extends ConfigurationPart {
            public int minSpawnDelay = 200;
            public int maxSpawnDelay = 800;
            public int spawnCount = 4;
            public int maxNearbyEntities = 6;
            public int requiredPlayerRange = 16;
            public int spawnRange = 4;
        }
    }
}
