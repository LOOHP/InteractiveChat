package com.loohp.interactivechat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.Utils.MaterialUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!label.equals("interactivechat") && !label.equals("ic")) {
			return true;
		}
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.AQUA + "InteractiveChat written by LOOHP!");
			sender.sendMessage(ChatColor.GOLD + "You are running InteractiveChat version: " + InteractiveChat.plugin.getDescription().getVersion());
			return true;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("interactivechat.reload")) {
				ConfigManager.reloadConfig();
				ConfigManager.loadConfig();
				MaterialUtils.reloadLang();
				sender.sendMessage(InteractiveChat.ReloadPlugin);
			} else {
				sender.sendMessage(InteractiveChat.NoPermission);
			}
		}
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args[0].equals("viewinv")) {
				long key = Long.parseLong(args[1]);
				if (InteractiveChat.inventoryDisplay.containsKey(key)) {
					Inventory inv = InteractiveChat.inventoryDisplay.get(key);
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.InvExpired));
				}
			} else if (args[0].equals("viewender")) {
				long key = Long.parseLong(args[1]);
				if (InteractiveChat.enderDisplay.containsKey(key)) {
					Inventory inv = InteractiveChat.enderDisplay.get(key);
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.InvExpired));
				}
			} else if (args[0].equals("viewitem")) {
				long key = Long.parseLong(args[1]);
				if (InteractiveChat.itemDisplay.containsKey(key)) {
					Inventory inv = InteractiveChat.itemDisplay.get(key);
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.InvExpired));
				}
			}
		}
		
		return true;
	}

}
