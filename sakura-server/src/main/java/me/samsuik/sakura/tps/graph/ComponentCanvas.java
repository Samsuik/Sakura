package me.samsuik.sakura.tps.graph;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public final class ComponentCanvas {
    private final int width;
    private final int height;
    private final Component[][] components;

    public ComponentCanvas(final int width, final int height) {
        this.width = width;
        this.height = height;
        // [x, y] is flipped as it makes converting the components into a list easier
        this.components = new Component[height][width];
    }

    public void flip() {
        for (int y = 0; y < this.height; ++y) {
            if (y >= this.height / 2) {
                final Component[] row = this.components[y];
                int relocatingRow = this.height - 1 - y;
                this.components[y] = this.components[relocatingRow];
                this.components[relocatingRow] = row;
            }
        }
    }

    public void fill(final Component component) {
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                this.set(x, y, component);
            }
        }
    }

    public Component get(final int x, final int y) {
        final Component component = this.components[y][x];
        return Preconditions.checkNotNull(component, "missing component at x:{} y:{}", x, y);
    }

    public void set(final int x, final int y, final Component component) {
        this.components[y][x] = component;
    }

    public BuiltComponentCanvas build() {
        return new BuiltComponentCanvas(this.joinComponents());
    }

    private List<Component> joinComponents() {
        final List<Component> componentList = new ObjectArrayList<>(this.height);
        for (final Component[] row : this.components) {
            componentList.add(Component.join(JoinConfiguration.noSeparators(), row));
        }
        return componentList;
    }
}
