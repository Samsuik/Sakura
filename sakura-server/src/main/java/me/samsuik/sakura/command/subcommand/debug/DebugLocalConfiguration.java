package me.samsuik.sakura.command.subcommand.debug;

import me.samsuik.sakura.command.PlayerOnlySubCommand;
import me.samsuik.sakura.configuration.local.ConfigurationContainer;
import me.samsuik.sakura.configuration.local.LocalConfigurationAccessor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public final class DebugLocalConfiguration extends PlayerOnlySubCommand {
    private static final int DEFAULT_REGION_SIZE = 16;

    public DebugLocalConfiguration(final String name) {
        super(name);
    }

    @Override
    public void execute(final Player player, final String[] args) {
        final Location location = player.getLocation();
        final LocalConfigurationAccessor localConfigurationAccessor = location.getWorld().localConfig();
        final List<BoundingBox> areas = new ArrayList<>(localConfigurationAccessor.getAreas(location));
        final BoundingBox boundingBox = areas.stream().findAny().orElse(null);

        if (boundingBox != null) {
            player.sendRichMessage("<yellow>You are currently inside a area with %s local-config(s).".formatted(areas.size()));
            for (int count = 0; count < Math.min(4, areas.size()); ++count) {
                if (count != 0) {
                    player.sendRichMessage("<yellow> |");
                }

                final BoundingBox area = areas.get(count);
                final ConfigurationContainer container = localConfigurationAccessor.get(area);

                player.sendRichMessage("<yellow> | X: %.0f -> %.0f".formatted(area.getMinX(), area.getMaxX()));
                player.sendRichMessage("<yellow> | Y: %.0f -> %.0f".formatted(area.getMinY(), area.getMaxY()));
                player.sendRichMessage("<yellow> | Z: %.0f -> %.0f".formatted(area.getMinZ(), area.getMaxZ()));
                player.sendRichMessage("<yellow> | ID: " + System.identityHashCode(container));
            }

            if (areas.size() > 4) {
                player.sendRichMessage("<yellow> | ... and %s more".formatted(areas.size() - 4));
            }

            player.sendRichMessage(localConfigurationAccessor.getAllAreas().size() + " total areas");
        }

        if (args.length == 0) {
            return;
        }

        if ("delete".equalsIgnoreCase(args[0]) && boundingBox != null) {
            localConfigurationAccessor.remove(boundingBox);
            player.sendRichMessage("<red>Removed area");
        }

        if ("create".equalsIgnoreCase(args[0]) && args.length > 1) {
            final int size = parseInt(args, 1).orElse(DEFAULT_REGION_SIZE);
            final BoundingBox area = BoundingBox.of(location, size, size, size);
            localConfigurationAccessor.set(area, ConfigurationContainer.sealedContainer());
            player.sendRichMessage("<green>Created a new area with size " + size);
        }

        if ("clear".equalsIgnoreCase(args[0])) {
            player.sendRichMessage("<red>Removed all areas");
            localConfigurationAccessor.removeAll();
        }

        if ("grid".equalsIgnoreCase(args[0]) && args.length > 2) {
            final int count = parseInt(args, 1).orElse(DEFAULT_REGION_SIZE) / 2;
            final int size  = parseInt(args, 2).orElse(DEFAULT_REGION_SIZE);
            final int adjustedSize = size + 16;

            for (int x = -count; x <= count; x++) {
                for (int z = -count; z <= count; z++) {
                    final int minX = (adjustedSize * x) - 16;
                    final int minZ = (adjustedSize * z) - 16;

                    final BoundingBox area = new BoundingBox(
                        minX,
                        Integer.MIN_VALUE,
                        minZ,
                        minX + size,
                        Integer.MAX_VALUE,
                        minZ + size
                    );

                    localConfigurationAccessor.set(area, ConfigurationContainer.sealedContainer());
                }
            }

            player.sendRichMessage("<green>Created a %sx%s grid with size %s".formatted(count, count, size));
        }
    }

    @Override
    public void tabComplete(final List<String> completions, final String[] args) throws IllegalArgumentException {
        completions.addAll(List.of("create", "delete", "clear", "grid"));
    }
}
