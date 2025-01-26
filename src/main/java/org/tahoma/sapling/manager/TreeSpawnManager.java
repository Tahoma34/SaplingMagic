package org.tahoma.sapling.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.tahoma.sapling.SaplingMagic;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class TreeSpawnManager {
    public static class SaplingData {
        private final Block saplingBlock;
        private int timer;
        private boolean isProcessed = false;

        public SaplingData(Block saplingBlock, int timer) {
            this.saplingBlock = saplingBlock;
            this.timer = timer;
        }

        public Block getSaplingBlock() {
            return saplingBlock;
        }

        public boolean isProcessed() {
            return isProcessed;
        }

        public void setProcessed(boolean processed) {
            this.isProcessed = processed;
        }
    }

    private static final Map<Location, SaplingData> saplingMap = new HashMap<>();

    public static SaplingData registerSapling(Block block, int timer) {
        SaplingData data = new SaplingData(block, timer);
        saplingMap.put(block.getLocation(), data);
        return data;
    }

    public static SaplingData getSaplingData(Block block) {
        return saplingMap.get(block.getLocation());
    }

    public static void loadSchematic(WorldEditPlugin wePlugin, Location loc, String fileName) {
        try {
            File schematicFile = new File(SaplingMagic.getInstance().getDataFolder(), "schematics/" + fileName);
            if (!schematicFile.exists()) {
                SaplingMagic.getInstance().getLogger().severe("Схематика не найдена: " + schematicFile.getAbsolutePath());
                return;
            }
            var format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                SaplingMagic.getInstance().getLogger().severe("Не удалось определить формат схематики!");
                return;
            }
            Clipboard clipboard = format.getReader(new FileInputStream(schematicFile)).read();
            if (loc.getWorld() == null) {
                SaplingMagic.getInstance().getLogger().severe("Мир для вставки схематики не найден!");
                return;
            }
            World weWorld = BukkitAdapter.adapt(loc.getWorld());
            try (EditSession editSession = wePlugin.getWorldEdit().newEditSession(weWorld)) {
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                Operation operation = holder.createPaste(editSession)
                        .to(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
                        .ignoreAirBlocks(false)
                        .build();

                Operations.complete(operation);
                editSession.close();
                SaplingMagic.getInstance().getLogger().info("Схема '" + fileName + "' вставлена корректно!");

                startSpiralEffects(loc);

            } catch (Exception e) {
                SaplingMagic.getInstance().getLogger().severe("Ошибка при вставке схемы: " + e.getMessage());
            }
        } catch (Exception e) {
            SaplingMagic.getInstance().getLogger().severe("Ошибка чтения файла схематики: " + e.getMessage());
        }
    }

    public static void startSpiralEffects(Location center) {
        if (center.getWorld() != null) {
            center.getWorld().playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
        new BukkitRunnable() {
            double radius = 0;
            double angle = 0;
            double currentY = center.getY();

            @Override
            public void run() {
                if (radius > 20) {
                    this.cancel();
                    return;
                }
                double x = center.getX() + radius * Math.cos(Math.toRadians(angle));
                double z = center.getZ() + radius * Math.sin(Math.toRadians(angle));
                currentY += 0.05;

                Location particleLocation = new Location(center.getWorld(), x, currentY, z);
                if (center.getWorld() != null) {
                    center.getWorld().playEffect(particleLocation, Effect.DRAGON_BREATH, 0);
                }
                angle += 15;
                radius += 0.1;
            }
        }.runTaskTimer(SaplingMagic.getInstance(), 0L, 2L);
    }
}