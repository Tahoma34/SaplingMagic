package org.tahoma.sapling.gui;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.tahoma.sapling.SaplingMagic;

public class ItemsGUIListener implements Listener {

    private final SaplingMagic plugin;

    public ItemsGUIListener(SaplingMagic plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String itemRemovedMsg = plugin.getConfig().getString("messages.itemRemoved", "&aПредмет успешно удалён!");

        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Управление предметами")) {
            event.setCancelled(true);

            Inventory inv = event.getClickedInventory();
            if (inv == null || event.getCurrentItem() == null) return;

            int slot = event.getSlot();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null) {
                plugin.getItemManager().removeItem(slot);

                event.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', itemRemovedMsg));
                event.getWhoClicked().closeInventory();
                ItemsGUI.open((org.bukkit.entity.Player) event.getWhoClicked());
            }
        }
    }
}
