package me.samsuik.sakura.command.subcommand;

import me.samsuik.sakura.command.PlayerOnlySubCommand;
import me.samsuik.sakura.configuration.GlobalConfiguration;
import me.samsuik.sakura.configuration.local.CachedLocalConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.core.BlockPos;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class MechanicCommand extends PlayerOnlySubCommand {
    private static final String MECHANIC_INFORMATION_MESSAGE = """
                Mechanic Version: <yellow><mechanic_version></yellow>
                Height Parity: <yellow><height_parity></yellow>
                Tnt Spread: <yellow><tnt_spread></yellow>
                Tnt Flow: <yellow><tnt_flow></yellow>
                Redstone Implementation: <yellow><redstone_implementation></yellow>
                Paper Optimize Explosions: <yellow><broken_explosion_behaviour></yellow>
                Consistent Radius: <yellow><consistent_radius></yellow>
                Lava Flow Speed: <yellow><lava_flow_speed></yellow>
                Floating Point Fix: <yellow><floating_point_fix></yellow>""";

    public MechanicCommand(final String name) {
        super(name);
        this.setAliases(List.of("mechanics", "mech"));
        this.description = "Displays information related to cannon mechanics";
    }

    @Override
    public void execute(final Player player, final String[] args) {
        final Location location = player.getLocation();
        final BlockPos blockPos = CraftLocation.toBlockPos(location);
        final CraftWorld craftWorld = ((CraftWorld) location.getWorld());
        final CachedLocalConfiguration config = craftWorld.getHandle().localConfig().at(blockPos);

        player.sendMessage(GlobalConfiguration.get().messages.mechanicInformationComponent(
            MiniMessage.miniMessage().deserialize(
                MECHANIC_INFORMATION_MESSAGE,
                Placeholder.unparsed("mechanic_version", config.mechanicsTarget.toString()),
                Placeholder.unparsed("height_parity", String.valueOf(this.hasHeightParity(location))),
                Placeholder.unparsed("tnt_spread", this.getTntSpread(location)),
                Placeholder.unparsed("tnt_flow", String.valueOf(this.hasTntFlow(location))),
                Placeholder.unparsed("redstone_implementation", config.redstoneBehaviour.implementation().getFriendlyName()),
                Placeholder.unparsed("broken_explosion_behaviour", String.valueOf(config.brokenPaperExplosionBehaviour)),
                Placeholder.unparsed("consistent_radius", String.valueOf(config.consistentExplosionRadius)),
                Placeholder.unparsed("lava_flow_speed", String.valueOf(config.lavaFlowSpeed)),
                Placeholder.unparsed("floating_point_fix", String.valueOf(config.floatingPointFix))
            )
        ));
    }

    private boolean hasHeightParity(final Location location) {
        final FallingBlock fallingBlock = this.createTestEntity(location, FallingBlock.class);
        return fallingBlock != null && fallingBlock.getHeightParity();
    }

    private String getTntSpread(final Location location) {
        final TNTPrimed tnt = this.createTestEntity(location, TNTPrimed.class);
        if (tnt == null) {
            return "unknown";
        }

        final Vector velocity = tnt.getVelocity();
        final String spread;
        if (velocity.getX() != 0.0 && velocity.getY() != 0.0) {
            spread = "ALL";
        } else if (velocity.getY() != 0.0) {
            spread = "Y";
        } else {
            spread = "NONE";
        }

        return spread;
    }

    private boolean hasTntFlow(final Location location) {
        final TNTPrimed tnt = this.createTestEntity(location, TNTPrimed.class);
        return tnt == null || tnt.isPushedByFluid();
    }

    private <T extends Entity> @Nullable T createTestEntity(final Location location, final Class<T> type) {
        final T entity = location.getWorld().createEntity(location, type);
        return new EntitySpawnEvent(entity).callEvent() ? entity : null;
    }
}
