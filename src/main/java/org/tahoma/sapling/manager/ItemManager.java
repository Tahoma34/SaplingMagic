package org.tahoma.sapling.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.tahoma.sapling.SaplingMagic;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemManager {
    private final SaplingMagic plugin;
    private final FileConfiguration itemsConfig;
    private final File itemsFile;

    public ItemManager(SaplingMagic plugin, FileConfiguration itemsConfig, File itemsFile) {
        this.plugin = plugin;
        this.itemsConfig = itemsConfig;
        this.itemsFile = itemsFile;
        loadItems();
    }

    public void loadItems() {
        plugin.getLogger().info("Загрузка предметов из items.yml...");
    }

    public void saveItems() {
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Не удалось сохранить items.yml! " + e.getMessage());
        }
    }

    public void addItem(String path, ItemStack item, double chance) {
        String base64 = ItemStackSerializer.itemStackToBase64(item);
        List<String> list = itemsConfig.getStringList(path);
        list.add(base64 + ";" + chance);
        itemsConfig.set(path, list);
        saveItems();
    }

    public void removeItem(int index) {
        List<String> list = itemsConfig.getStringList("items");
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            itemsConfig.set("items", list);
            saveItems();
        }
    }

    public List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        List<String> itemDataList = itemsConfig.getStringList("items");
        for (String itemData : itemDataList) {
            try {
                String[] parts = itemData.split(";");
                if (parts.length > 0) {
                    ItemStack item = ItemStackSerializer.itemStackFromBase64(parts[0]);
                    items.add(item);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при загрузке предмета: " + itemData + ". " + e.getMessage());
            }
        }
        return items;
    }

    public List<ItemWithChance> getItemsWithChances() {
        List<ItemWithChance> result = new ArrayList<>();
        List<String> itemDataList = itemsConfig.getStringList("items");
        for (String itemData : itemDataList) {
            try {
                String[] parts = itemData.split(";");
                if (parts.length == 2) {
                    ItemStack item = ItemStackSerializer.itemStackFromBase64(parts[0]);
                    double chance = Double.parseDouble(parts[1]);
                    result.add(new ItemWithChance(item, chance));
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при загрузке предмета: " + itemData + ". " + e.getMessage());
            }
        }
        return result;
    }

    public static class ItemWithChance {
        private final ItemStack item;
        private final double chance;

        public ItemWithChance(ItemStack item, double chance) {
            this.item = item;
            this.chance = chance;
        }

        public ItemStack getItem() {
            return item;
        }

        public double getChance() {
            return chance;
        }
    }


    public void giveDropTimerItem(Player player, int singleDropInterval, Location treeLocation) {
        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int remaining = singleDropInterval;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                double distance = player.getLocation().distance(treeLocation);
                if (distance > 50) {
                    return;
                }
                if (remaining <= 0) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent("§6До окончания дропа: " + remaining + "с")
                );
                remaining--;
            }
        }, 0L, 20L);
    }
}