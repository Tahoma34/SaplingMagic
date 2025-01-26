package org.tahoma.sapling.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.tahoma.sapling.SaplingMagic;

import java.util.List;
import java.util.Random;

public class TreeDropManager {
    private final Plugin plugin;
    private int taskId = -1;
    private int droppedSoFar = 0;

    public TreeDropManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public int startDropTask(Location center) {
        final int dropInterval = plugin.getConfig().getInt("settings.dropInterval", 10);
        final int dropRadius = plugin.getConfig().getInt("settings.dropRadius", 5);
        final int maxDropAmount = plugin.getConfig().getInt("settings.dropAmount", 2);
        droppedSoFar = 0;
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) <= 50) {
                ((SaplingMagic) plugin).getItemManager().giveDropTimerItem(player, dropInterval * maxDropAmount, center);
            }
        }
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                () -> {
                    if (droppedSoFar >= maxDropAmount) {
                        stopDropTask();
                        return;
                    }
                    dropSingleItem(center, dropRadius);
                    droppedSoFar++;
                },
                0L,
                dropInterval * 20L
        );
        return taskId;
    }

    public void stopDropTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void dropSingleItem(Location center, int dropRadius) {
        List<ItemStack> allItems = ((SaplingMagic) plugin).getItemManager().getItems();
        if (allItems == null || allItems.isEmpty()) {
            return;
        }
        Random random = new Random();
        ItemStack chosenItem = allItems.get(random.nextInt(allItems.size()));
        double angle = 2 * Math.PI * random.nextDouble();
        double r = dropRadius * Math.sqrt(random.nextDouble());
        double offsetX = r * Math.cos(angle);
        double offsetZ = r * Math.sin(angle);
        Location dropLocation = center.clone().add(offsetX, 1.0, offsetZ);
        if (center.getWorld() != null) {
            Item droppedItem = center.getWorld().dropItemNaturally(dropLocation, chosenItem);
            droppedItem.setGlowing(true);
        }
    }
}