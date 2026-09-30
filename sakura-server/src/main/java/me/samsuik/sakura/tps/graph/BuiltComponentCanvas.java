package me.samsuik.sakura.tps.graph;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Supplier;

@NullMarked
public final class BuiltComponentCanvas {
    private final List<Component> components;

    BuiltComponentCanvas(final List<Component> components) {
        this.components = components;
    }

    public void appendLeft(final Supplier<Component> componentSupplier) {
        this.components.replaceAll(row -> componentSupplier.get().append(row));
    }

    public void appendLeft(final Component component) {
        this.components.replaceAll(component::append);
    }

    public void appendRight(final Supplier<Component> componentSupplier) {
        this.components.replaceAll(component -> component.append(componentSupplier.get()));
    }

    public void appendRight(final Component component) {
        this.components.replaceAll(row -> row.append(component));
    }

    public void header(final Component component) {
        this.components.addFirst(component);
    }

    public void footer(final Component component) {
        this.components.add(component);
    }

    public int rows() {
        return this.components.size();
    }

    public ObjectImmutableList<Component> components() {
        return new ObjectImmutableList<>(this.components);
    }
}
