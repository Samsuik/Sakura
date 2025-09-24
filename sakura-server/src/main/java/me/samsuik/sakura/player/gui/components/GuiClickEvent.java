package me.samsuik.sakura.player.gui.components;

import me.samsuik.sakura.player.gui.FeatureGuiInventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GuiClickEvent {
    void doSomething(final InventoryClickEvent event, final FeatureGuiInventory inventory);
}
