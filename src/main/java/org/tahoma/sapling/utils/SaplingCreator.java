package org.tahoma.sapling.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SaplingCreator {

    // Эта строка остаётся в качестве скрытой метки, чтобы отличить "настоящий" предмет от поддельного
    private static final String PLUGIN_MARKER = ChatColor.DARK_GRAY + " ";


    public static ItemStack createMagicSapling(FileConfiguration config) {
        String path = "sapling.";
        String materialName = config.getString(path + "material", "OAK_SAPLING");
        String displayName = config.getString(path + "name", "&dМагический саженец");
        List<String> loreFromConfig = config.getStringList(path + "lore");

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            Bukkit.getLogger().warning("Некорректный материал в конфиге: " + materialName + ", используем OAK_SAPLING");
            material = Material.OAK_SAPLING;
        }

        ItemStack sapling = new ItemStack(material, 1);
        ItemMeta meta = sapling.getItemMeta();
        if (meta == null) {
            return sapling;
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        List<String> lore = new ArrayList<>();
        if (loreFromConfig != null && !loreFromConfig.isEmpty()) {
            for (String line : loreFromConfig) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        lore.add(PLUGIN_MARKER);

        meta.setLore(lore);
        sapling.setItemMeta(meta);

        return sapling;
    }


    public static boolean isMagicSapling(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null) {
            return false;
        }

        return meta.getLore().contains(PLUGIN_MARKER);
    }
}
