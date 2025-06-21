package me.samsuik.sakura.player.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ItemStackUtil {
    public static ItemStack itemWithBlankName(Material material) {
        return itemWithName(material, Component.empty());
    }

    public static ItemStack itemWithName(Material material, Component component) {
        ItemStack item = new ItemStack(material);
        item.editMeta(m -> m.itemName(component));
        return item;
    }
}
