package me.samsuik.sakura.player.gui.components;

import com.google.common.base.Preconditions;
import me.samsuik.sakura.player.gui.FeatureGuiInventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.List;

@NullMarked
public final class ItemSwitch implements GuiComponent {
    private final List<ItemStack> items;
    private final int slot;
    private final int selected;
    private final GuiClickEvent whenClicked;

    public ItemSwitch(final List<ItemStack> items, final int slot, final int selected, final GuiClickEvent whenClicked) {
        Preconditions.checkArgument(!items.isEmpty());
        this.items = Collections.unmodifiableList(items);
        this.slot = slot;
        this.selected = selected;
        this.whenClicked = whenClicked;
    }

    @Override
    public boolean interaction(final InventoryClickEvent event, final FeatureGuiInventory featureInventory) {
        if (this.slot == event.getSlot()) {
            final int next = (this.selected + 1) % this.items.size();
            final ItemSwitch itemSwitch = new ItemSwitch(this.items, this.slot, next, this.whenClicked);
            featureInventory.replaceComponent(this, itemSwitch);
            this.whenClicked.doSomething(event, featureInventory);
            return true;
        }
        return false;
    }

    @Override
    public void creation(final Inventory inventory) {
        inventory.setItem(this.slot, this.items.get(this.selected));
    }
}
