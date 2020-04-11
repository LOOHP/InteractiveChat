package com.loohp.interactivechat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.interactivechat.Updater.Updater;
import com.loohp.interactivechat.Utils.MaterialUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!label.equalsIgnoreCase("interactivechat") && !label.equalsIgnoreCase("ic")) {
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
			return true;
		}
		
		if (args[0].equalsIgnoreCase("update")) {
			if (sender.hasPermission("interactivechat.update")) {
				sender.sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat written by LOOHP!");
				sender.sendMessage(ChatColor.GOLD + "[InteractiveChat] You are running InteractiveChat version: " + InteractiveChat.plugin.getDescription().getVersion());
				new BukkitRunnable() {
					public void run() {
						String version = Updater.checkUpdate();
						if (version.equals("latest")) {
							sender.sendMessage(ChatColor.GREEN + "[InteractiveChat] You are running the latest version!");
						} else {
							Updater.sendUpdateMessage(version);
						}
					}
				}.runTaskAsynchronously(InteractiveChat.plugin);
			} else {
				sender.sendMessage(InteractiveChat.NoPermission);
			}
			return true;
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
				return true;
			} else if (args[0].equals("viewender")) {
				long key = Long.parseLong(args[1]);
				if (InteractiveChat.enderDisplay.containsKey(key)) {
					Inventory inv = InteractiveChat.enderDisplay.get(key);
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.InvExpired));
				}
				return true;
			} else if (args[0].equals("viewitem")) {
				long key = Long.parseLong(args[1]);
				if (InteractiveChat.itemDisplay.containsKey(key)) {
					Inventory inv = InteractiveChat.itemDisplay.get(key);
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.InvExpired));
				}
				return true;
			}
		}
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Bukkit.spigot().getConfig().getString("messages.unknown-command")));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> tab = new ArrayList<String>();
		if (!label.equalsIgnoreCase("interactivechat") && !label.equalsIgnoreCase("ic")) {
			return tab;
		}
		
		if (args.length <= 1) {
			if (sender.hasPermission("interactivechat.reload")) {
				tab.add("reload");
			}
			if (sender.hasPermission("interactivechat.update")) {
				tab.add("update");
			}
			return tab;
		}
		return tab;
	}

}
