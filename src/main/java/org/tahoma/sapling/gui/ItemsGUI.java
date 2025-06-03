package org.tahoma.sapling.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tahoma.sapling.SaplingMagic;
import org.tahoma.sapling.manager.ItemManager.ItemWithChance;

import java.util.ArrayList;
import java.util.List;

public class ItemsGUI {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Управление предметами");

        List<ItemWithChance> itemsWithChances = SaplingMagic.getInstance().getItemManager().getItemsWithChances();

        int index = 0;
        for (ItemWithChance iwc : itemsWithChances) {
            if (index >= 54) break;

            ItemStack item = iwc.getItem();
            double chance = iwc.getChance();

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                int roundedChance = (int) Math.round(chance);
                lore.add(ChatColor.GRAY + "Шанс: " + roundedChance + "%");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(index, item);
            index++;
        }

        player.openInventory(inv);
    }
}
