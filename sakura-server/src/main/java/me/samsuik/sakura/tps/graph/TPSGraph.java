package me.samsuik.sakura.tps.graph;

import com.google.common.base.Preconditions;
import me.samsuik.sakura.tps.ServerTickInformation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public abstract class TPSGraph {
    protected final List<ServerTickInformation> tickInformation;
    protected final int width;
    protected final int height;
    protected final double scale;

    public TPSGraph(final int width, final int height, final double scale, final List<ServerTickInformation> tickInformation) {
        Preconditions.checkArgument(tickInformation.size() == width);
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.tickInformation = tickInformation;
    }

    public abstract BuiltComponentCanvas plot();

    protected final int rowFromColumn(final int x) {
        final int clamped = Math.clamp(x, 0, this.width - 1);
        final ServerTickInformation tickInformation = this.tickInformation.get(clamped);
        return this.rowFromTPS(tickInformation.tps());
    }

    protected final int rowFromTPS(final double tps) {
        final int row = Mth.floor((tps / 3) * this.scale);
        return Mth.clamp(row, 0, this.height - 1);
    }

    protected final void addColourAndHoverInformation(final ComponentCanvas canvas) {
        for (int x = 0; x < this.width; ++x) {
            final ServerTickInformation tickInformation = this.tickInformation.get(x);
            final TextColor colourFromTPS = tickInformation.colour();
            final Component hoverComponent = tickInformation.hoverComponent(colourFromTPS);
            final HoverEvent<Component> hoverEvent = HoverEvent.showText(hoverComponent);

            for (int y = 0; y < this.height; ++y) {
                Component component = canvas.get(x, y);
                if (component == GraphComponents.BACKGROUND) {
                    component = component.color(NamedTextColor.BLACK);
                } else {
                    component = component.color(colourFromTPS);
                }
                canvas.set(x, y, component.hoverEvent(hoverEvent));
            }
        }
    }
}
