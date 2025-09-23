package me.samsuik.sakura.command.subcommands.debug;

import me.samsuik.sakura.command.PlayerOnlySubCommand;
import me.samsuik.sakura.configuration.local.ConfigurationContainer;
import me.samsuik.sakura.configuration.local.LocalConfigurationAccessor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jspecify.annotations.NullMarked;

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
        final BoundingBox boundingBox = localConfigurationAccessor.getAreas(location).stream()
            .findAny()
            .orElse(null);

        if (boundingBox != null) {
            player.sendRichMessage("<green>You are currently inside a area with a set local-config.");
            player.sendRichMessage("<green> - %.0f %.0f %.0f".formatted(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ()));
            player.sendRichMessage("<green> - %.0f %.0f %.0f".formatted(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxX()));
        }

        if (args.length == 0) {
            return;
        }

        if ("delete".equalsIgnoreCase(args[0]) && boundingBox != null) {
            localConfigurationAccessor.remove(boundingBox);
            player.sendRichMessage("<green>Removed area");
        }

        if ("create".equalsIgnoreCase(args[0]) && args.length > 1) {
            final int size = parseInt(args, 1).orElse(DEFAULT_REGION_SIZE);
            final BoundingBox area = BoundingBox.of(location, size, size, size);
            localConfigurationAccessor.set(area, ConfigurationContainer.sealedContainer());
            player.sendRichMessage("<green>Created a new area with size " + size);
        }
    }

    @Override
    public void tabComplete(final List<String> completions, final String[] args) throws IllegalArgumentException {
        completions.addAll(List.of("create", "delete"));
    }
}
