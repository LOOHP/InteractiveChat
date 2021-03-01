package com.loohp.interactivechat;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.API.InteractiveChatAPI;
import com.loohp.interactivechat.BungeeMessaging.BungeeMessageSender;
import com.loohp.interactivechat.Data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.Listeners.MapViewer;
import com.loohp.interactivechat.Modules.CustomPlaceholderDisplay;
import com.loohp.interactivechat.Modules.EnderchestDisplay;
import com.loohp.interactivechat.Modules.InventoryDisplay;
import com.loohp.interactivechat.Modules.ItemDisplay;
import com.loohp.interactivechat.Modules.PlayernameDisplay;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.ICPlayer;
import com.loohp.interactivechat.Updater.Updater;
import com.loohp.interactivechat.Updater.Updater.UpdaterResponse;
import com.loohp.interactivechat.Utils.ChatColorUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

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
				if (InteractiveChat.bungeecordMode) {
					try {
						BungeeMessageSender.reloadBungeeConfig();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				sender.sendMessage(InteractiveChat.reloadPluginMessage);
			} else {
				sender.sendMessage(InteractiveChat.noPermissionMessage);
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
				sender.sendMessage(InteractiveChat.noPermissionMessage);
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
						sender.sendMessage(InteractiveChat.noConsoleMessage);
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
							sender.sendMessage(InteractiveChat.invalidPlayerMessage);
						}
					} else {
						sender.sendMessage(InteractiveChat.noPermissionMessage);
					}
				}
			} else {
				sender.sendMessage(InteractiveChat.noPermissionMessage);
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("list")) {
			if (sender.hasPermission("interactivechat.list")) {
				if (sender.hasPermission("interactivechat.list.all")) {
					InteractiveChatAPI.sendMessageUnprocessed(sender, InteractiveChat.listPlaceholderHeader);
					String body = InteractiveChat.listPlaceholderBody;
					int i = 0;
					for (ICPlaceholder placeholder : InteractiveChat.placeholderList) {
						i++;
						String text = body.replace("{Order}", i + "").replace("{Keyword}", placeholder.getKeyword()).replace("{Description}", placeholder.getDescription());
						InteractiveChatAPI.sendMessageUnprocessed(sender, text);
					}
				} else {
					InteractiveChatAPI.sendMessageUnprocessed(sender, InteractiveChat.listPlaceholderHeader);
					String body = InteractiveChat.listPlaceholderBody;
					int i = 0;
					for (ICPlaceholder placeholder : InteractiveChat.placeholderList) {
						if ((placeholder.isBuildIn() && sender.hasPermission(placeholder.getPermission())) || (!placeholder.isBuildIn() && (sender.hasPermission(placeholder.getPermission()) || !InteractiveChat.useCustomPlaceholderPermissions))) {
							i++;
							String text = body.replace("{Order}", i + "").replace("{Keyword}", placeholder.getKeyword()).replace("{Description}", placeholder.getDescription());
							InteractiveChatAPI.sendMessageUnprocessed(sender, text);
						}
					}
				}
			} else {
				sender.sendMessage(InteractiveChat.noPermissionMessage);
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("parse")) {
			if (sender.hasPermission("interactivechat.parse")) {
				if (sender instanceof Player) {
					String str = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
					Player player = (Player) sender;
					Optional<ICPlayer> icplayer = Optional.of(new ICPlayer(player));
					Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
						try {
							long unix = System.currentTimeMillis();
							BaseComponent baseComponent = new TextComponent(str);
							if (InteractiveChat.usePlayerName) {
								baseComponent = PlayernameDisplay.process(baseComponent, icplayer, unix);
					        }
					        if (InteractiveChat.useItem) {
					        	baseComponent = ItemDisplay.processWithoutCooldown(baseComponent, icplayer, player, unix);
					        }
					        if (InteractiveChat.useInventory) {
					        	baseComponent = InventoryDisplay.processWithoutCooldown(baseComponent, icplayer, player, unix);
					        }
					        if (InteractiveChat.useEnder) {
					        	baseComponent = EnderchestDisplay.processWithoutCooldown(baseComponent, icplayer, player, unix);
					        }
					        baseComponent = CustomPlaceholderDisplay.process(baseComponent, icplayer, player, InteractiveChat.placeholderList, unix, true);
					        
					        InteractiveChatAPI.sendMessageUnprocessed(sender, baseComponent);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				} else {
					sender.sendMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
				}
			} else {
				sender.sendMessage(InteractiveChat.noPermissionMessage);
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("lengthtest") && sender.hasPermission("interactivechat.debug")) {
			Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
				try {
					int length = args.length < 2 ? 5000 : Integer.parseInt(args[1]);
					String str = "";
					for (int i = 0; i < length; i++) {
						str += (i % 2) == 0 ? (ChatColor.GOLD + "n") : (ChatColor.YELLOW + "a");
					}
					sender.spigot().sendMessage(new TextComponent(str));
				} catch (Exception e) {
					sender.sendMessage(e.getMessage());
				}
			});
			return true;
		}
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args[0].equals("viewinv")) {
				Inventory inv = InteractiveChat.inventoryDisplay.get(args[1]);
				if (inv != null) {
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.invExpiredMessage));
				}
			} else if (args[0].equals("viewender")) {
				Inventory inv = InteractiveChat.enderDisplay.get(args[1]);
				if (inv != null) {
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.invExpiredMessage));
				}
			} else if (args[0].equals("viewitem")) {
				Inventory inv = InteractiveChat.itemDisplay.get(args[1]);
				if (inv != null) {
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.invExpiredMessage));
				}
			} else if (args[0].equals("viewmap")) {
				ItemStack map = InteractiveChat.mapDisplay.get(args[1]);
				if (map != null) {
					Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> MapViewer.showMap(player, map));
				} else {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.invExpiredMessage));
				}
			}
			return true;
		}
		
		sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', Bukkit.spigot().getConfig().getString("messages.unknown-command")));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> tab = new LinkedList<>();
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
			if (sender.hasPermission("interactivechat.list")) {
				tab.add("list");
			}
			if (sender.hasPermission("interactivechat.parse")) {
				tab.add("parse");
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
			if (sender.hasPermission("interactivechat.list")) {
				if ("list".startsWith(args[0].toLowerCase())) {
					tab.add("list");
				}
			}
			if (sender.hasPermission("interactivechat.parse")) {
				if ("parse".startsWith(args[0].toLowerCase())) {
					tab.add("parse");
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
