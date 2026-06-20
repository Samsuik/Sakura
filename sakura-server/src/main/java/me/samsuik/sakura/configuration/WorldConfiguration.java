package me.samsuik.sakura.configuration;

import io.papermc.paper.configuration.Configuration;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.NestedSetting;
import io.papermc.paper.configuration.PaperConfigurations;
import io.papermc.paper.configuration.type.Duration;
import io.papermc.paper.configuration.type.number.DoubleOr;
import io.papermc.paper.configuration.type.number.IntOr;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.samsuik.sakura.SakuraFeatureHooks;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.explosion.durable.DurableMaterial;
import me.samsuik.sakura.mechanics.MinecraftMechanicsTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "NotNullFieldNotInitialized", "InnerClassMayBeStatic", "RedundantSuppression"})
public final class WorldConfiguration extends ConfigurationPart {
    static final int CURRENT_VERSION = 12;

    private transient final Identifier worldIdentifier;
    WorldConfiguration(final Identifier worldIdentifier) {
        this.worldIdentifier = worldIdentifier;
    }

    public boolean isDefault() {
        return this.worldIdentifier.equals(PaperConfigurations.WORLD_DEFAULTS_KEY);
    }

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Cannons cannons;
    public final class Cannons extends ConfigurationPart {
        public MergeLevel mergeLevel = MergeLevel.LENIENT;
        public boolean tntAndSandAffectedByBubbleColumns = true;

        @NestedSetting({"treat-collidable-blocks-as-full", "while-moving"})
        public boolean treatAllBlocksAsFullWhenMoving = false;
        @NestedSetting({"treat-collidable-blocks-as-full", "moving-faster-than"})
        public double treatAllBlocksAsFullWhenMovingFasterThan = 64.0;
        public boolean loadChunks = false;

        public Restrictions restrictions = new Restrictions();
        public final class Restrictions extends ConfigurationPart {
            @Comment("The amount of blocks that can be travelled before changing direction is restricted")
            public IntOr.Disabled leftShootingThreshold = IntOr.Disabled.DISABLED;
            @Comment("The maximum amount of blocks that a cannon can adjust")
            public IntOr.Disabled maxAdjustDistance = IntOr.Disabled.DISABLED;
            @Comment("The maximum amount of falling blocks that can fall instantly")
            public IntOr.Disabled instantBlockFallLimit = IntOr.Disabled.DISABLED;
        }

        public Tnt tnt = new Tnt();
        public final class Tnt extends ConfigurationPart {
            public boolean forcePositionUpdates;
        }

        public Sand sand = new Sand();
        public final class Sand extends ConfigurationPart {
            public boolean despawnInsideMovingPistons = true;
            public boolean concreteSolidifyInWater = true;

            @NestedSetting({"prevent-stacking", "against-border"})
            public boolean preventAgainstBorder = false;
            @NestedSetting({"prevent-stacking", "world-height"})
            public boolean preventAtWorldHeight = false;
            public boolean dropItems = true;
        }

        public Explosion explosion = new Explosion();
        public final class Explosion extends ConfigurationPart {
            public boolean optimiseProtectedRegions = false;
            public boolean avoidRedundantBlockSearches = false;
            public boolean reuseBlockCacheAcrossExplosions = false;
            public boolean batchExplosions = true;
            public boolean reduceExposureRaycasts = false;

            public Map<Block, DurableMaterial> durableMaterials = Util.make(new Reference2ObjectOpenHashMap<>(), map -> {
                map.put(Blocks.OBSIDIAN, new DurableMaterial(4, Blocks.COBBLESTONE.getExplosionResistance(), true));
                map.put(Blocks.ANVIL, new DurableMaterial(3, Blocks.END_STONE.getExplosionResistance(), true));
                map.put(Blocks.CHIPPED_ANVIL, new DurableMaterial(3, Blocks.END_STONE.getExplosionResistance(), true));
                map.put(Blocks.DAMAGED_ANVIL, new DurableMaterial(3, Blocks.END_STONE.getExplosionResistance(), true));
            });

            public Duration durableMaterialsExpiration = Duration.of("1m");

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

            @PostProcess
            private void postProcess() {
                SakuraFeatureHooks.setDurableMaterialExpiration(WorldConfiguration.this.worldIdentifier, this.durableMaterialsExpiration);
            }
        }

        public Mechanics mechanics = new Mechanics();
        public final class Mechanics extends ConfigurationPart {
            public TNTSpread tntSpread = TNTSpread.ALL;
            public boolean tntFlowsInWater = true;
            public boolean fallingBlockParity = false;
            public MinecraftMechanicsTarget mechanicsTarget = MinecraftMechanicsTarget.latest();
            public boolean fallingBlockFloatingPointFix = false;

            @Comment(
                "Replaces the optimize-explosions option in the paper config." +
                "In Sakura it's a misleading option that hurts performance and breaks cannons."
            )
            public boolean brokenPaperExplosionBehaviour = false;

            public boolean useBrokenPaperExplosionBehaviour(final Level level) {
                return level.paperConfig().environment.optimizeExplosions && this.mechanicsTarget.isLegacy()
                    || this.brokenPaperExplosionBehaviour;
            }

            public enum TNTSpread {
                ALL, Y, NONE;
            }
        }
    }

    public Technical technical;
    public final class Technical extends ConfigurationPart {
        public boolean dispenserRandomItemSelection = true;
        @Comment(
            "Only tick hoppers when items are able to be moved\n" +
            "This can cause issues with redstone contraptions that rely on DUD's to detect when hoppers fail to move items."
        )
        public boolean optimiseIdleHopperTicking = true;

        public Redstone redstone = new Redstone();
        public final class Redstone extends ConfigurationPart {
            public boolean redstoneCache = true;
            public boolean fluidsBreakRedstone = true;
        }

        @Comment(
            "Allow TNT duplication while `allow-piston-duplication` is disabled.\n" +
            "This exists so servers can enable TNT duplication without reintroducing the other forms of piston duplication."
        )
        public boolean allowTntDuplication = false;
    }

    public Players players;
    public final class Players extends ConfigurationPart {
        public Combat combat = new Combat();
        public final class Combat extends ConfigurationPart {
            public boolean legacyCombatMechanics = false;
            public boolean allowSweepAttacks = true;
            public boolean shieldDamageReduction = false;
            public boolean oldPotionEffects = false;
            public boolean oldEnchantedGoldenApple = false;
            public boolean oldSoundsAndParticleEffects = false;
            public boolean fastHealthRegen = true;

            @Comment(
                "A cooldown for the lunge enchantment in milliseconds.\n" +
                "\"disabled\" disables the lunge enchantment, 0 is for no delay (vanilla)."
            )
            public IntOr.Disabled lungeCooldown = new IntOr.Disabled(OptionalInt.of(0));

            @Comment(
                "The maximum damage a player can take in a single hit.\n" +
                "This can prevent arrows and maces instantly killing players."
            )
            public DoubleOr.Disabled maxDamage = DoubleOr.Disabled.DISABLED;
            public IntOr.Default maxArmourDamage = IntOr.Default.USE_DEFAULT;
            public Map<Item, Double> itemAttackDamageOverride = new HashMap<>();

            @PostProcess
            public void postProcess() {
                // Nerf the lunge enchantment when legacy combat is enabled
                if (this.lungeCooldown.enabled() && this.legacyCombatMechanics) {
                    final int cooldown = Math.max(650, this.lungeCooldown.or(-1));
                    this.lungeCooldown = new IntOr.Disabled(OptionalInt.of(cooldown));
                }
            }
        }

        public Knockback knockback = new Knockback();
        public final class Knockback extends ConfigurationPart {
            public DoubleOr.Default knockbackVertical = DoubleOr.Default.USE_DEFAULT;
            public double knockbackVerticalLimit = 0.4;
            public boolean verticalKnockbackRequireGround = true;
            public double baseKnockback = 0.4;
            @Comment("Knockback caused by sweeping edge")
            public double sweepingEdgeKnockback = 0.4;

            public Sprinting sprinting = new Sprinting();
            public final class Sprinting extends ConfigurationPart {
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
        public boolean collideWithCobwebs = false;
    }

    public Entity entity;
    public final class Entity extends ConfigurationPart {
        @Comment("Only modify if you know what you're doing")
        public boolean disableMobAi = false;
        public boolean waterSensitivity = true;
        public boolean instantDeathAnimation = false;
        public boolean ironGolemsTakeFalldamage = false;
        public boolean insertItemsIntoHoppersOnDeath = false;
        public boolean nerfedMobsCanPushEntities = false;

        public Items items = new Items();
        public final class Items extends ConfigurationPart {
            public BlastResistant blastResistant = new BlastResistant();
            public final class BlastResistant extends ConfigurationPart {
                public Set<Item> items = Set.of();
                public boolean whitelistOverBlacklist = true;
            }

            public ExplosionItemDrops explosionItemDrops = new ExplosionItemDrops();
            public final class ExplosionItemDrops extends ConfigurationPart {
                public Set<Item> items = Set.of();
                public boolean whitelistOverBlacklist = false;
            }
        }

        @Comment("Entity travel distance limits")
        public Map<EntityType<?>, Integer> chunkTravelLimit = Util.make(new Reference2ObjectOpenHashMap<>(), map -> {
            map.put(EntityType.ENDER_PEARL, 8);
        });

        public ThrownPotion thrownPotion = new ThrownPotion();
        public final class ThrownPotion extends ConfigurationPart {
            public double horizontalSpeed = 1.0;
            public double verticalSpeed = 1.0;
            public boolean allowBreakingInsideEntities = false;
            public boolean disableRelativePotionVelocity = false;
        }

        public EnderPearl enderPearl = new EnderPearl();
        public final class EnderPearl extends ConfigurationPart {
            public boolean useOutlineForCollision = false;
            public boolean preventTeleportingInsideBlocks = false;
            public boolean slowedDownByWater = true;
            public boolean randomSpreadWhenThrown = true;
        }
    }

    public Environment environment;
    public final class Environment extends ConfigurationPart {
        public boolean allowWaterInTheNether = false;
        public boolean disableFastNetherLava = false;
        public boolean disableFluidsFlowingThroughTheWorldBorder = false;

        public BlockGeneration blockGeneration = new BlockGeneration();
        public final class BlockGeneration extends ConfigurationPart {
            public boolean legacyBlockFormation = false;
        }

        public Crops crops = new Crops();
        public final class Crops extends ConfigurationPart {
            public boolean useRandomChanceToGrow = false;
            public IntOr.Default minCactusFlowerGrowthHeight = IntOr.Default.USE_DEFAULT;
        }

        public MobSpawner mobSpawner = new MobSpawner();
        public final class MobSpawner extends ConfigurationPart {
            public boolean checkSpawnConditions = true;
            public boolean requireNearbyPlayer = true;
            public boolean ignoreEntityLimit = false;
        }
    }
}
