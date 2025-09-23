package me.samsuik.sakura.configuration;

import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.Configuration;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.type.number.IntOr;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.slf4j.Logger;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@SuppressWarnings({"CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "NotNullFieldNotInitialized", "InnerClassMayBeStatic", "RedundantSuppression"})
public final class GlobalConfiguration extends ConfigurationPart {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    static final int CURRENT_VERSION = 3;// (when you change the version, change the comment, so it conflicts on rebases): rename filter bad nbt from spawn eggs

    private static GlobalConfiguration instance;
    public static GlobalConfiguration get() {
        return instance;
    }

    static void set(GlobalConfiguration instance) {
        GlobalConfiguration.instance = instance;
    }

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Messages messages;
    public class Messages extends ConfigurationPart {
        public String durableBlockInteraction = "<dark_gray>(<light_purple>S</light_purple>) <white>This block has <gray><remaining></gray> of <gray><durability>";
        public String fpsSettingChange = "<dark_gray>(<light_purple>S</light_purple>) <gray><state> <yellow><name>";
        public boolean tpsShowEntityAndChunkCount = true;

        public Component durableBlockInteractionComponent(final int remaining, final int durability) {
            return MiniMessage.miniMessage().deserialize(
                GlobalConfiguration.get().messages.durableBlockInteraction,
                Placeholder.unparsed("remaining", String.valueOf(remaining)),
                Placeholder.unparsed("durability", String.valueOf(durability))
            );
        }
    }

    public Fps fps;
    public class Fps extends ConfigurationPart {
        public Material material = Material.PINK_STAINED_GLASS_PANE;
    }

    public Players players;
    public class Players extends ConfigurationPart {
        public Combat combat = new Combat();
        public class Combat extends ConfigurationPart {
            public boolean blockWithSwords = false;
        }

        public IntOr.Default bucketStackSize = IntOr.Default.USE_DEFAULT;
        public boolean stackableMilkBuckets = false;
    }

    public Environment environment;
    public class Environment extends ConfigurationPart {
        @Comment("This is only intended for plot worlds. Will affect chunk generation on servers.")
        public boolean calculateBiomeNoiseOncePerChunkSection = false;

        public MobSpawnerDefaults mobSpawnerDefaults = new MobSpawnerDefaults();
        public class MobSpawnerDefaults extends ConfigurationPart {
            public int minSpawnDelay = 200;
            public int maxSpawnDelay = 800;
            public int spawnCount = 4;
            public int maxNearbyEntities = 6;
            public int requiredPlayerRange = 16;
            public int spawnRange = 4;
        }
    }

}
