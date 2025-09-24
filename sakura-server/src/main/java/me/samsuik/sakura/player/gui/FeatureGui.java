package me.samsuik.sakura.player.gui;

import me.samsuik.sakura.player.gui.components.GuiComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class FeatureGui {
    private final int size;
    private final Component title;

    public FeatureGui(final int size, final Component title) {
        this.size = size;
        this.title = title;
    }

    protected abstract void fillInventory(final Inventory inventory);

    protected abstract void afterFill(final Player player, final FeatureGuiInventory inventory);

    public final void showTo(final Player bukkitPlayer) {
        final FeatureGuiInventory featureInventory = new FeatureGuiInventory(this, this.size, this.title);
        this.fillInventory(featureInventory.getInventory());
        this.afterFill(bukkitPlayer, featureInventory);
        bukkitPlayer.openInventory(featureInventory.getInventory());
    }

    @ApiStatus.Internal
    public static void clickEvent(final InventoryClickEvent event) {
        final Inventory clicked = event.getClickedInventory();
        if (clicked != null && clicked.getHolder(false) instanceof FeatureGuiInventory featureInventory) {
            event.setCancelled(true); // cancel the event first to let components uncancel the event if desired.
            for (final GuiComponent component : featureInventory.getComponents().reversed()) {
                if (component.interaction(event, featureInventory)) {
                    break;
                }
            }
        }
    }
}
