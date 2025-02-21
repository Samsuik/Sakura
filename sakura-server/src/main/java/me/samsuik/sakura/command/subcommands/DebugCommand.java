package me.samsuik.sakura.command.subcommands;

import me.samsuik.sakura.command.BaseSubCommand;
import me.samsuik.sakura.redstone.RedstoneNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public final class DebugCommand extends BaseSubCommand {
    public DebugCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player) || args.length == 0) {
            return;
        }

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        if (args[0].equalsIgnoreCase("redstone-cache")) {
            this.showCachedWires(player, nmsPlayer.level());
        }
    }

    @Override
    public void tabComplete(List<String> list, String[] args) throws IllegalArgumentException {
        list.add("redstone-cache");
    }

    private void showCachedWires(Player player, Level level) {
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
