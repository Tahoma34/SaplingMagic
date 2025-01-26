package org.tahoma.sapling;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.tahoma.sapling.commands.MSaplingCommand;
import org.tahoma.sapling.events.SaplingListener;
import org.tahoma.sapling.gui.ItemsGUIListener;
import org.tahoma.sapling.manager.ItemManager;
import org.tahoma.sapling.manager.TreeDropManager;
import org.tahoma.sapling.manager.TreeSpawnManager;

import java.io.File;

public final class SaplingMagic extends JavaPlugin {

    private static SaplingMagic instance;
    private WorldEditPlugin worldEditPlugin;

    private File itemsFile;
    private FileConfiguration itemsConfig;
    private ItemManager itemManager;

    private TreeDropManager dropManager;
    private int dropTaskId = -1;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        createItemsConfig();

        itemManager = new ItemManager(this, itemsConfig, itemsFile);

        MSaplingCommand msaplingCmd = new MSaplingCommand(this);
        if (getCommand("msapling") != null) {
            getCommand("msapling").setExecutor(msaplingCmd);
            getCommand("msapling").setTabCompleter(msaplingCmd);
        }

        getServer().getPluginManager().registerEvents(new SaplingListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemsGUIListener(this), this);

        File schematicsFolder = new File(SaplingMagic.getInstance().getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        worldEditPlugin = (WorldEditPlugin) Bukkit
                .getServer()
                .getPluginManager()
                .getPlugin("WorldEdit");
        if (worldEditPlugin != null && worldEditPlugin.isEnabled()) {
            getLogger().info("WorldEdit загружен и подключён!");
        } else {
            getLogger().severe("WorldEdit не найден! Плагин может работать некорректно.");
        }

        dropManager = new TreeDropManager(this);

        removeLeftoverTimerStands();

        getLogger().info(ChatColor.GREEN + "SaplingMagic включён!");
    }

    @Override
    public void onDisable() {
        if (dropTaskId != -1) {
            dropManager.stopDropTask();
        }
        itemManager.saveItems();

        getLogger().info(ChatColor.RED + "SaplingMagic отключён!");
    }

    private void removeLeftoverTimerStands() {
        Bukkit.getWorlds().forEach(world -> {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    ArmorStand stand = (ArmorStand) entity;
                    String name = stand.getCustomName();
                    if (name != null && name.contains("Осталось:")) {
                        stand.remove();
                    }
                }
            }
        });
    }

    private void createItemsConfig() {
        itemsFile = new File(getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            itemsFile.getParentFile().mkdirs();
            saveResource("items.yml", false);
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public void spawnTimerArmorStand(TreeSpawnManager.SaplingData data, int totalSeconds) {
        Location location = data.getSaplingBlock().getLocation().add(0.5, 0, 0.5);

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setVisible(false);

        final int[] taskIdHolder = new int[1];

        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            private int remainingTime = totalSeconds;

            @Override
            public void run() {
                if (data.getSaplingBlock().getType() != Material.OAK_SAPLING) {
                    armorStand.remove();
                    Bukkit.getScheduler().cancelTask(taskIdHolder[0]);
                    return;
                }

                if (remainingTime <= 0) {
                    armorStand.remove();

                    if (!data.isProcessed()) {
                        data.setProcessed(true);

                        if (data.getSaplingBlock().getType() == Material.OAK_SAPLING) {
                            Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                                try {
                                    TreeSpawnManager.loadSchematic(
                                            getWorldEditPlugin(),
                                            data.getSaplingBlock().getLocation(),
                                            "tree.schem"
                                    );
                                    dropTaskId = dropManager.startDropTask(
                                            data.getSaplingBlock().getLocation().add(0.5, 0, 0.5)
                                    );
                                } catch (Exception e) {
                                    getLogger().severe("Ошибка при вставке схемы: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }, 1L);
                        }
                    }
                    Bukkit.getScheduler().cancelTask(taskIdHolder[0]);
                } else {
                    armorStand.setCustomName(ChatColor.GREEN + "Осталось: " + remainingTime + " с.");
                    remainingTime--;
                }
            }
        }, 0L, 20L);
    }

    public void updateConfigValues() {
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public TreeDropManager getDropManager() {
        return dropManager;
    }

    public static SaplingMagic getInstance() {
        return instance;
    }
}