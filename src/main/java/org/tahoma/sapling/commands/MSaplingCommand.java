package org.tahoma.sapling.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tahoma.sapling.SaplingMagic;
import org.tahoma.sapling.gui.ItemsGUI;
import org.tahoma.sapling.manager.ItemManager;
import org.tahoma.sapling.utils.SaplingCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MSaplingCommand implements CommandExecutor, TabCompleter {

    private final SaplingMagic plugin;

    public MSaplingCommand(SaplingMagic plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!sender.hasPermission("msapling.use")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав использовать эту команду!");
            return true;
        }


        String notPlayerMsg      = plugin.getConfig().getString("messages.notPlayer", "&cТолько игрок может использовать эту команду!");
        String usageMsg          = plugin.getConfig().getString("messages.usage", "&eИспользование: /msapling <give|additem|items|reload> ...");
        String specifyNickMsg    = plugin.getConfig().getString("messages.specifyNickname", "&cУкажите ник: /msapling give <nick>");
        String playerNotFoundMsg = plugin.getConfig().getString("messages.playerNotFound", "&cИгрок не найден!");
        String noItemInHandMsg   = plugin.getConfig().getString("messages.noItemInHand", "&cУ вас в руке нет предмета!");
        String itemAddedMsg      = plugin.getConfig().getString("messages.itemAdded", "&aПредмет добавлен в items.yml с шансом %chance%");
        String chanceRangeMsg    = plugin.getConfig().getString("messages.chanceRange", "&cШанс должен быть от 1 до 100!");
        String unknownSubCmdMsg  = plugin.getConfig().getString("messages.unknownSubcommand", "&eНеизвестная подкоманда. Используйте /msapling help.");

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', notPlayerMsg));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', usageMsg));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!player.hasPermission("msapling.give")) {
                    player.sendMessage(ChatColor.RED + "У вас нет прав выдать магический саженец!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', specifyNickMsg));
                    return true;
                }
                Player target = plugin.getServer().getPlayerExact(args[1]);
                if (target != null) {
                    target.getInventory().addItem(SaplingCreator.createMagicSapling(plugin.getConfig()));
                    player.sendMessage(ChatColor.GREEN + "Вы выдали магический саженец игроку " + target.getName());
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                }
            }
            case "additem" -> {
                if (!player.hasPermission("msapling.additem")) {
                    player.sendMessage(ChatColor.RED + "У вас нет прав добавлять предметы!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Используйте: /msapling additem <шанс>");
                    return true;
                }
                if (player.getInventory().getItemInMainHand().getType().isAir()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', noItemInHandMsg));
                    return true;
                }
                double chance;
                try {
                    chance = Double.parseDouble(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Неверный формат шанса. Укажите число от 1 до 100.");
                    return true;
                }
                if (chance < 1 || chance > 100) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', chanceRangeMsg));
                    return true;
                }
                ItemManager itemManager = plugin.getItemManager();
                itemManager.addItem("items", player.getInventory().getItemInMainHand(), chance);
                String finalMsg = itemAddedMsg.replace("%chance%", String.valueOf(chance));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', finalMsg));
            }
            case "items" -> {
                if (!player.hasPermission("msapling.items")) {
                    player.sendMessage(ChatColor.RED + "У вас нет прав открывать GUI предметов!");
                    return true;
                }
                ItemsGUI.open(player);
            }
            case "reload" -> {
                if (!player.hasPermission("msapling.reload")) {
                    player.sendMessage(ChatColor.RED + "У вас нет прав перезагружать конфигурацию!");
                    return true;
                }
                plugin.reloadConfig();
                plugin.updateConfigValues();
                player.sendMessage(ChatColor.GREEN + "Конфигурация плагина успешно перезагружена.");
            }
            default -> {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', unknownSubCmdMsg));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!sender.hasPermission("msapling.use")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("msapling.give")) {
                subCommands.add("give");
            }
            if (sender.hasPermission("msapling.additem")) {
                subCommands.add("additem");
            }
            if (sender.hasPermission("msapling.items")) {
                subCommands.add("items");
            }
            if (sender.hasPermission("msapling.reload")) {
                subCommands.add("reload");
            }

            String currentArg = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String subCmd : subCommands) {
                if (subCmd.startsWith(currentArg)) {
                    result.add(subCmd);
                }
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("msapling.give")) {
            List<String> playerNames = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                playerNames.add(p.getName());
            }
            String currentArg = args[1].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String name : playerNames) {
                if (name.toLowerCase().startsWith(currentArg)) {
                    result.add(name);
                }
            }
            return result;
        }

        return Collections.emptyList();
    }
}
