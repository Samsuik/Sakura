package me.samsuik.sakura.command.subcommands;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import me.samsuik.sakura.command.BaseSubCommand;
import me.samsuik.sakura.tps.ServerTickInformation;
import me.samsuik.sakura.tps.graph.BuiltComponentCanvas;
import me.samsuik.sakura.tps.graph.DetailedTPSGraph;
import me.samsuik.sakura.tps.graph.GraphComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class TPSCommand extends BaseSubCommand {
    private static final int GRAPH_WIDTH = 71;
    private static final int GRAPH_HEIGHT = 10;
    private static final Style GRAY_WITH_STRIKETHROUGH = Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH);

    public TPSCommand(final String name) {
        super(name);
        this.description = "Displays the current ticks per second";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        final ServerTickInformation tickInformation = MinecraftServer.getServer().latestTickInformation();
        final long identifier = this.parseLong(args, 1).orElse(tickInformation.identifier());
        double scale = this.parseDouble(args, 0).orElse(-1.0);
        if (scale < 0.0) {
            // Scale the tps graph to the current server tps
            scale = this.dynamicScale(identifier);
        }

        final ObjectImmutableList<ServerTickInformation> tickHistory = MinecraftServer.getServer().tickHistory(identifier - GRAPH_WIDTH, identifier);
        final DetailedTPSGraph graph = new DetailedTPSGraph(GRAPH_WIDTH, GRAPH_HEIGHT, scale, tickHistory);
        final BuiltComponentCanvas canvas = graph.plot();

        // Add the sidebars, header and footer
        canvas.appendLeft(Component.text(":", NamedTextColor.BLACK));
        canvas.appendRight(Component.text(":", NamedTextColor.BLACK));
        canvas.header(this.createHeaderComponent(tickInformation, identifier));
        canvas.footer(Component.text("*", NamedTextColor.DARK_GRAY)
            .append(Component.text(Strings.repeat(" ", GRAPH_WIDTH - 1), GRAY_WITH_STRIKETHROUGH))
            .append(Component.text("*")));

        for (final Component component : canvas.components()) {
            sender.sendMessage(component);
        }
    }

    private double dynamicScale(final long identifier) {
        final ObjectImmutableList<ServerTickInformation> tickHistory = MinecraftServer.getServer().tickHistory(identifier - 5, identifier);
        final double averageTps = tickHistory.stream()
            .mapToDouble(ServerTickInformation::tps)
            .average()
            .orElse(0.0);
        return 20.0 / averageTps;
    }

    private Component createHeaderComponent(final ServerTickInformation tickInformation, final long identifier) {
        final int scrollAmount = GRAPH_WIDTH / 3 * 2;
        final double memoryUsage = memoryUsage();
        final TextComponent.Builder builder = Component.text();
        builder.color(NamedTextColor.DARK_GRAY);
        builder.append(Component.text("< ")
            .clickEvent(ClickEvent.runCommand("/tps -1 " + (identifier + scrollAmount))));
        builder.append(Component.text(Strings.repeat(" ", 19), GRAY_WITH_STRIKETHROUGH));
        builder.append(Component.text(" ( "));
        builder.append(Component.text("Now: ", NamedTextColor.WHITE)
            .append(Component.text("%.1f".formatted(tickInformation.tps()), tickInformation.colour())));
        builder.appendSpace();
        builder.append(Component.text("Mem: ", NamedTextColor.WHITE)
            .append(Component.text("%.1f".formatted(memoryUsage * 100), GraphComponents.colour(1 - (float) memoryUsage))));
        builder.append(Component.text("% ) "));
        builder.append(Component.text(Strings.repeat(" ", 18), GRAY_WITH_STRIKETHROUGH));
        builder.append(Component.text(" >")
            .clickEvent(ClickEvent.runCommand("/tps -1 " + (identifier - scrollAmount))));
        return builder.build();
    }

    private static double memoryUsage() {
        final Runtime runtime = Runtime.getRuntime();
        final double free  = runtime.freeMemory();
        final double max   = runtime.maxMemory();
        final double alloc = runtime.totalMemory();
        return (alloc - free) / max;
    }
}
