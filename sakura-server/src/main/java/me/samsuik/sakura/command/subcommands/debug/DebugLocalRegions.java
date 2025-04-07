package me.samsuik.sakura.command.subcommands.debug;

import me.samsuik.sakura.command.BaseSubCommand;
import me.samsuik.sakura.local.LocalRegion;
import me.samsuik.sakura.local.storage.LocalStorageHandler;
import me.samsuik.sakura.local.storage.LocalValueStorage;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public final class DebugLocalRegions extends BaseSubCommand {
    private static final int DEFAULT_REGION_SIZE = 16;

    public DebugLocalRegions(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            this.sendPlayerOnlyMessage(sender);
            return;
        }

        final Location location = player.getLocation();
        final World world = location.getWorld();
        final LocalStorageHandler storageHandler = world.getStorageHandler();
        final int blockX = location.getBlockX();
        final int blockZ = location.getBlockZ();
        final Optional<LocalRegion> currentRegion = storageHandler.locate(blockX, blockZ);

        if ("create".equalsIgnoreCase(args[0])) {
            final int size = parseInt(args, 1).orElse(DEFAULT_REGION_SIZE);
            final LocalRegion region = LocalRegion.at(blockX, blockZ, size);
            storageHandler.put(region, new LocalValueStorage());
        }

        if ("get".equalsIgnoreCase(args[0])) {
            sender.sendRichMessage("<red>" + (currentRegion.isPresent() ? currentRegion.get() : "not inside of a region"));
        }

        if (currentRegion.isPresent()) {
            final LocalRegion region = currentRegion.get();
            if ("delete".equalsIgnoreCase(args[0])) {
                storageHandler.remove(region);
            }
        }
    }
}
