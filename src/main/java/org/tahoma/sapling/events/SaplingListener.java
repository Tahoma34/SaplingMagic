package org.tahoma.sapling.events;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.tahoma.sapling.SaplingMagic;
import org.tahoma.sapling.manager.TreeSpawnManager;

public class SaplingListener implements Listener {

    private final SaplingMagic plugin;
    private final int treeSpawnTimer;

    private final boolean sendChatMessage;
    private final boolean sendHotbarMessage;

    public SaplingListener(SaplingMagic plugin) {
        this.plugin = plugin;
        this.treeSpawnTimer = plugin.getConfig().getInt("settings.treeSpawnTimer", 40);

        this.sendChatMessage = plugin.getConfig().getBoolean("messages.enableChat", true);
        this.sendHotbarMessage = plugin.getConfig().getBoolean("messages.enableHotbar", true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.OAK_SAPLING) {
            Block saplingBlock = event.getBlockPlaced();
            TreeSpawnManager.SaplingData data = TreeSpawnManager.registerSapling(saplingBlock, treeSpawnTimer);
            plugin.spawnTimerArmorStand(data, treeSpawnTimer);

            String messageTemplate = plugin.getConfig().getString(
                    "messages.saplingPlaced",
                    "&aИгрок %player% посадил волшебный саженец на координатах &eX:%x% &eY:%y% &eZ:%z%."
            );

            String intermediateMessage = messageTemplate
                    .replace("%player%", event.getPlayer().getName())
                    .replace("%x%", String.valueOf(saplingBlock.getX()))
                    .replace("%y%", String.valueOf(saplingBlock.getY()))
                    .replace("%z%", String.valueOf(saplingBlock.getZ()));

            String finalMessage = ChatColor.translateAlternateColorCodes('&', intermediateMessage);

            if (sendChatMessage) {
                Bukkit.getServer().broadcastMessage(finalMessage);
            }

            if (sendHotbarMessage) {
                final String hotbarMessage = finalMessage;
                Bukkit.getOnlinePlayers().forEach(onlinePlayer ->
                        onlinePlayer.spigot().sendMessage(
                                ChatMessageType.ACTION_BAR,
                                new TextComponent(hotbarMessage)
                        )
                );
            }
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (event.getBlock().getType() == Material.OAK_SAPLING) {
            TreeSpawnManager.SaplingData saplingData = TreeSpawnManager.getSaplingData(event.getBlock());
            if (saplingData != null && !saplingData.isProcessed()) {
                event.setCancelled(true);
            }
        }
    }
}