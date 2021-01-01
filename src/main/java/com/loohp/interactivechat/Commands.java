package com.loohp.interactivechat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.BungeeMessaging.BungeeMessageSender;
import com.loohp.interactivechat.Data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.Updater.Updater;
import com.loohp.interactivechat.Updater.Updater.UpdaterResponse;
import com.loohp.interactivechat.Utils.ChatColorUtils;
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
				MaterialUtils.reloadLang();
				if (InteractiveChat.bungeecordMode) {
					try {
						BungeeMessageSender.reloadBungeeConfig();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
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
				Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
					UpdaterResponse version = Updater.checkUpdate();
					if (version.getResult().equals("latest")) {
						if (version.isDevBuildLatest()) {
							sender.sendMessage(ChatColor.GREEN + "[InteractiveChat] You are running the latest version!");
						} else {
							Updater.sendUpdateMessage(sender, version.getResult(), version.getSpigotPluginId(), true);
						}
					} else {
						Updater.sendUpdateMessage(sender, version.getResult(), version.getSpigotPluginId());
					}
				});
			} else {
				sender.sendMessage(InteractiveChat.NoPermission);
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("mentiontoggle")) {
			if (sender.hasPermission("interactivechat.mention.toggle")) {
				if (args.length == 1) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						PlayerData pd = InteractiveChat.playerDataManager.getPlayerData(player);
						if (pd.isMentionDisabled()) {
							pd.setMentionDisabled(false);
							pd.saveConfig();
							sender.sendMessage(InteractiveChat.mentionEnable);
						} else {
							pd.setMentionDisabled(true);
							pd.saveConfig();
							sender.sendMessage(InteractiveChat.mentionDisable);
						}
						if (InteractiveChat.bungeecordMode) {
							try {
								BungeeMessageSender.forwardPlayerDataUpdate(player.getUniqueId(), pd.getConfig());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else {
						sender.sendMessage(InteractiveChat.Console);
					}
				} else {
					if (sender.hasPermission("interactivechat.mention.toggle.others")) {
						Player player = Bukkit.getPlayer(args[1]);
						if (player != null) {
							PlayerData pd = InteractiveChat.playerDataManager.getPlayerData(player);
							if (pd.isMentionDisabled()) {
								pd.setMentionDisabled(false);
								pd.saveConfig();
								sender.sendMessage(InteractiveChat.mentionEnable);
							} else {
								pd.setMentionDisabled(true);
								pd.saveConfig();
								sender.sendMessage(InteractiveChat.mentionDisable);
							}
							if (InteractiveChat.bungeecordMode) {
								try {
									BungeeMessageSender.forwardPlayerDataUpdate(player.getUniqueId(), pd.getConfig());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} else {
							sender.sendMessage(InteractiveChat.InvalidPlayer);
						}
					} else {
						sender.sendMessage(InteractiveChat.NoPermission);
					}
				}
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
		
		sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', Bukkit.spigot().getConfig().getString("messages.unknown-command")));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> tab = new ArrayList<String>();
		if (!label.equalsIgnoreCase("interactivechat") && !label.equalsIgnoreCase("ic")) {
			return tab;
		}
		
		switch (args.length) {
		case 0:
			if (sender.hasPermission("interactivechat.reload")) {
				tab.add("reload");
			}
			if (sender.hasPermission("interactivechat.update")) {
				tab.add("update");
			}
			if (sender.hasPermission("interactivechat.mention.toggle")) {
				tab.add("mentiontoggle");
			}
			return tab;
		case 1:
			if (sender.hasPermission("interactivechat.reload")) {
				if ("reload".startsWith(args[0].toLowerCase())) {
					tab.add("reload");
				}
			}
			if (sender.hasPermission("interactivechat.update")) {
				if ("update".startsWith(args[0].toLowerCase())) {
					tab.add("update");
				}
			}
			if (sender.hasPermission("interactivechat.mention.toggle")) {
				if ("mentiontoggle".startsWith(args[0].toLowerCase())) {
					tab.add("mentiontoggle");
				}
			}
			return tab;
		case 2:
			if (sender.hasPermission("interactivechat.mention.toggle.others")) {
				if ("mentiontoggle".equalsIgnoreCase(args[0])) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getName().toLowerCase().startsWith(args[1])) {
							tab.add(player.getName());
						}
					}
				}
			}
			return tab;
		default:
			return tab;
		}
	}

}
