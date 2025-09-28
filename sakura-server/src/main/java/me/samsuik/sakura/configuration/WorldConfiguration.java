package me.samsuik.sakura.configuration;

import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.Configuration;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.NestedSetting;
import io.papermc.paper.configuration.PaperConfigurations;
import io.papermc.paper.configuration.type.number.DoubleOr;
import io.papermc.paper.configuration.type.number.IntOr;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.explosion.durable.DurableMaterial;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;
import java.util.Set;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "NotNullFieldNotInitialized", "InnerClassMayBeStatic", "RedundantSuppression"})
public final class WorldConfiguration extends ConfigurationPart {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    static final int CURRENT_VERSION = 12; // (when you change the version, change the comment, so it conflicts on rebases): rename filter bad nbt from spawn eggs

    private transient final ResourceLocation worldKey;
    WorldConfiguration(ResourceLocation worldKey) {
        this.worldKey = worldKey;
    }

    public boolean isDefault() {
        return this.worldKey.equals(PaperConfigurations.WORLD_DEFAULTS_KEY);
    }

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Cannons cannons;
    public class Cannons extends ConfigurationPart {
        public MergeLevel mergeLevel = MergeLevel.STRICT;
        public boolean tntAndSandAffectedByBubbleColumns = true;

        @NestedSetting({"treat-collidable-blocks-as-full", "while-moving"})
        public boolean treatAllBlocksAsFullWhenMoving = false;
        @NestedSetting({"treat-collidable-blocks-as-full", "moving-faster-than"})
        public double treatAllBlocksAsFullWhenMovingFasterThan = 64.0;
        public boolean loadChunks = false;

        public Restrictions restrictions = new Restrictions();
        public class Restrictions extends ConfigurationPart {
            @Comment("The amount of blocks that can be travelled before changing direction is restricted")
            public IntOr.Disabled leftShootingThreshold = IntOr.Disabled.DISABLED;
            @Comment(
                "Maximum amount of blocks that a cannon can adjust\n" +
                "It is recommended that this value kept sane and is more than 64 blocks"
            )
            public IntOr.Disabled maxAdjustDistance = IntOr.Disabled.DISABLED;
        }

        public Tnt tnt = new Tnt();
        public class Tnt extends ConfigurationPart {
            public boolean forcePositionUpdates;
        }

        public Sand sand = new Sand();
        public class Sand extends ConfigurationPart {
            public boolean despawnInsideMovingPistons = true;
            public boolean concreteSolidifyInWater = true;

            @NestedSetting({"prevent-stacking", "against-border"})
            public boolean preventAgainstBorder = false;
            @NestedSetting({"prevent-stacking", "world-height"})
            public boolean preventAtWorldHeight = false;
            public boolean dropItems = true;
        }

        public Explosion explosion = new Explosion();
        public class Explosion extends ConfigurationPart {
            public boolean optimiseProtectedRegions = false;
            public boolean avoidRedundantBlockSearches = false;
            public boolean reuseBlockCacheAcrossExplosions = false;

            public Map<Block, DurableMaterial> durableMaterials = Util.make(new Reference2ObjectOpenHashMap<>(), map -> {
                map.put(Blocks.OBSIDIAN, new DurableMaterial(4, Blocks.COBBLESTONE.getExplosionResistance(), true));
                map.put(Blocks.ANVIL, new DurableMaterial(3, Blocks.END_STONE.getExplosionResistance(), true));
                map.put(Blocks.CHIPPED_ANVIL, new DurableMaterial(3, Blocks.END_STONE.getExplosionResistance(), true));
                map.put(Blocks.DAMAGED_ANVIL, new DurableMaterial(3, Blocks.END_STONE.getExplosionResistance(), true));
            });

            public boolean protectScaffoldingFromCreepers = false;
            public boolean destroyWaterloggedBlocks = false;
            public boolean explodeLava = false;
            public boolean consistentRadius = false;
            public boolean explosionsHurtPlayers = true;
            public boolean explosionsDropItems = true;
            public boolean breakBlocksWhenOutsideTheWorldBorder = true;

            @Comment(
                "Protects blocks above the configured height from explosions.\n" +
                "Can be used to replicate the regen caps found on complexmc, and\n" +
                "useful for protecting the nether roof when bedrock is a durable-material."
            )
            public IntOr.Disabled protectBlocksAboveY = IntOr.Disabled.DISABLED;
        }

        public Mechanics mechanics = new Mechanics();
        public class Mechanics extends ConfigurationPart {
            public TNTSpread tntSpread = TNTSpread.ALL;
            public boolean tntFlowsInWater = true;
            public boolean fallingBlockParity = false;
            public MinecraftMechanicsTarget mechanicsTarget = MinecraftMechanicsTarget.latest();

            public enum TNTSpread {
                ALL, Y, NONE;
            }
        }
    }

    public Technical technical;
    public class Technical extends ConfigurationPart {
        public boolean dispenserRandomItemSelection = true;
        @Comment(
            "Only tick hoppers when items are able to be moved\n" +
            "This can cause issues with redstone contraptions that rely on DUD's to detect when hoppers fail to move items."
        )
        public boolean optimiseIdleHopperTicking = true;

        public Redstone redstone = new Redstone();
        public class Redstone extends ConfigurationPart {
            public boolean redstoneCache = false;
            public boolean fluidsBreakRedstone = true;
        }

        @Comment(
            "Allow TNT duplication while `allow-piston-duplication` is disabled.\n" +
            "This exists so servers can enable TNT duplication without reintroducing the other forms of piston duplication."
        )
        public boolean allowTntDuplication = false;
    }

    public Players players;
    public class Players extends ConfigurationPart {
        public Combat combat = new Combat();
        public class Combat extends ConfigurationPart {
            public boolean legacyCombatMechanics = false;
            public boolean allowSweepAttacks = true;
            public boolean shieldDamageReduction = false;
            public boolean oldEnchantedGoldenApple = false;
            public boolean oldSoundsAndParticleEffects = false;
            public boolean fastHealthRegen = true;
            public IntOr.Default maxArmourDamage = IntOr.Default.USE_DEFAULT;
        }

        public Knockback knockback = new Knockback();
        public class Knockback extends ConfigurationPart {
            public DoubleOr.Default knockbackVertical = DoubleOr.Default.USE_DEFAULT;
            public double knockbackVerticalLimit = 0.4;
            public boolean verticalKnockbackRequireGround = true;
            public double baseKnockback = 0.4;
            @Comment("Knockback caused by sweeping edge")
            public double sweepingEdgeKnockback = 0.4;

            public Sprinting sprinting = new Sprinting();
            public class Sprinting extends ConfigurationPart {
                public boolean requireFullAttack = true;
                public double extraKnockback = 0.5;
                @Comment("Delay between extra knockback hits in milliseconds")
                public IntOr.Default knockbackDelay = IntOr.Default.USE_DEFAULT;
            }

            @NestedSetting({"projectiles", "fishing-hooks-apply-knockback"})
            public boolean fishingHooksApplyKnockback;

            @Comment("Knockback resistance attribute modifier")
            public double knockbackResistanceModifier = 1.0;
            @Comment("Received by attacking a shielded enemy")
            public double shieldHitKnockback = 0.5;
        }

        @Comment("Prevents players swimming, gliding or using riptide to enter small holes")
        public boolean posesShrinkCollisionBox = true;
        public boolean fishingHooksPullEntities = true;
        public boolean preventPlacingSpawnEggsInsideBlocks = false;
    }

    public Entity entity;
    public class Entity extends ConfigurationPart {
        @Comment("Only modify if you know what you're doing")
        public boolean disableMobAi = false;
        public boolean waterSensitivity = true;
        public boolean instantDeathAnimation = false;
        public boolean ironGolemsTakeFalldamage = false;

        public Items items = new Items();
        public class Items extends ConfigurationPart {
            public BlastResistant blastResistant = new BlastResistant();
            public class BlastResistant extends ConfigurationPart {
                public Set<Item> items = Set.of();
                public boolean whitelistOverBlacklist = true;
            }

            public ExplosionItemDrops explosionItemDrops = new ExplosionItemDrops();
            public class ExplosionItemDrops extends ConfigurationPart {
                public Set<Item> items = Set.of();
                public boolean whitelistOverBlacklist = false;
            }
        }

        @Comment("Entity travel distance limits")
        public Map<EntityType<?>, Integer> chunkTravelLimit = Util.make(new Reference2ObjectOpenHashMap<>(), map -> {
            map.put(EntityType.ENDER_PEARL, 8);
        });

        public ThrownPotion thrownPotion = new ThrownPotion();
        public class ThrownPotion extends ConfigurationPart {
            public double horizontalSpeed = 1.0;
            public double verticalSpeed = 1.0;
            public boolean allowBreakingInsideEntities = false;
        }

        public EnderPearl enderPearl = new EnderPearl();
        public class EnderPearl extends ConfigurationPart {
            public boolean useOutlineForCollision = false;
            public boolean preventTeleportingInsideBlocks = false;
            public boolean slowedDownByWater = true;
            public boolean randomSpreadWhenThrown = true;
        }
    }

    public Environment environment;
    public class Environment extends ConfigurationPart {
        public boolean allowWaterInTheNether = false;
        public boolean disableFastNetherLava = false;
        public boolean disableFluidsFlowingThroughTheWorldBorder = false;

        public BlockGeneration blockGeneration = new BlockGeneration();
        public class BlockGeneration extends ConfigurationPart {
            public boolean legacyBlockFormation = false;
        }

        public Crops crops = new Crops();
        public class Crops extends ConfigurationPart {
            public boolean useRandomChanceToGrow = false;
        }

        public MobSpawner mobSpawner = new MobSpawner();
        public class MobSpawner extends ConfigurationPart {
            public boolean checkSpawnConditions = true;
            public boolean requireNearbyPlayer = true;
            public boolean ignoreEntityLimit = false;
        }
    }

}
