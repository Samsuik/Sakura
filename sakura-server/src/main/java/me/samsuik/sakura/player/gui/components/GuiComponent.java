package me.samsuik.sakura.player.gui.components;

import me.samsuik.sakura.player.gui.FeatureGuiInventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GuiComponent {
    boolean interaction(final InventoryClickEvent event, final FeatureGuiInventory featureInventory);

    void creation(final Inventory inventory);
}
