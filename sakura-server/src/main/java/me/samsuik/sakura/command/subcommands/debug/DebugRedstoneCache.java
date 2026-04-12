package me.samsuik.sakura.command.subcommands.debug;

import me.samsuik.sakura.command.PlayerOnlySubCommand;
import me.samsuik.sakura.redstone.cache.RedstoneNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public final class DebugRedstoneCache extends PlayerOnlySubCommand {
    public DebugRedstoneCache(final String name) {
        super(name);
    }

    @Override
    public void execute(final Player player, final String[] args) {
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final Level level = nmsPlayer.level();
        final Set<Location> redstoneWires = new HashSet<>();

        // Display randomly coloured wool blocks in place of redstone wires in a network.
        for (final RedstoneNetwork network : level.redstoneWireCache.getNetworkCache().values()) {
            final byte randomColour = (byte) ThreadLocalRandom.current().nextInt(16);
            final DyeColor dyeColour = DyeColor.getByWoolData(randomColour);
            final Material material = Material.matchMaterial(dyeColour.name() + "_WOOL");

            if (!network.isRegistered()) {
                continue;
            }

            for (final BlockPos pos : network.getWirePositions()) {
                final Location location = CraftLocation.toBukkit(pos, level);
                if (player.getLocation().distance(location) >= 64.0) {
                    continue;
                }
                player.sendBlockChange(location, material.createBlockData());
                redstoneWires.add(location);
            }
        }

        player.sendRichMessage("<red>Displaying %dx cached redstone wires".formatted(redstoneWires.size()));

        level.levelTickScheduler.runLater((t, l) -> {
            for (final Location loc : redstoneWires) {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }, 1200);
    }
}
