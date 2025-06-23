package me.samsuik.sakura.command.subcommands.debug;

import me.samsuik.sakura.command.PlayerOnlySubCommand;
import me.samsuik.sakura.redstone.RedstoneNetwork;
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
    public DebugRedstoneCache(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, String[] args) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        Level level = nmsPlayer.level();
        Set<Location> locations = new HashSet<>();
        for (RedstoneNetwork network : level.redstoneWireCache.getNetworkCache().values()) {
            byte randomColour = (byte) ThreadLocalRandom.current().nextInt(16);
            DyeColor dyeColour = DyeColor.getByWoolData(randomColour);
            Material material = Material.matchMaterial(dyeColour.name() + "_WOOL");

            if (!network.isRegistered()) {
                continue;
            }

            for (BlockPos pos : network.getWirePositions()) {
                Location location = CraftLocation.toBukkit(pos, level);
                if (player.getLocation().distance(location) >= 64.0) {
                    continue;
                }
                player.sendBlockChange(location, material.createBlockData());
                locations.add(location);
            }
        }

        player.sendRichMessage("<red>Displaying %dx cached redstone wires".formatted(locations.size()));

        level.levelTickScheduler.delayedTask(() -> {
            for (Location loc : locations) {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }, 1200);
    }
}
