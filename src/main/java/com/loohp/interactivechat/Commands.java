package com.loohp.interactivechat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.listeners.MapViewer;
import com.loohp.interactivechat.modules.CommandsDisplay;
import com.loohp.interactivechat.modules.CustomPlaceholderDisplay;
import com.loohp.interactivechat.modules.EnderchestDisplay;
import com.loohp.interactivechat.modules.InventoryDisplay;
import com.loohp.interactivechat.modules.ItemDisplay;
import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.updater.Updater;
import com.loohp.interactivechat.updater.Updater.UpdaterResponse;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
				InteractiveChat.itemDisplay.clear();
				InteractiveChat.inventoryDisplay.clear();
				InteractiveChat.inventoryDisplay1Lower.clear();
				InteractiveChat.enderDisplay.clear();
				InteractiveChat.mapDisplay.clear();
				InteractiveChat.itemDisplayTimeouts.clear();
				Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> InteractiveChat.playerDataManager.reload());
				if (InteractiveChat.bungeecordMode) {
					try {
						BungeeMessageSender.reloadBungeeConfig(System.currentTimeMillis());
					} catch (Exception e) {
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
							Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> pd.save());
							sender.sendMessage(InteractiveChat.mentionEnable);
						} else {
							pd.setMentionDisabled(true);
							Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> pd.save());
							sender.sendMessage(InteractiveChat.mentionDisable);
						}
						if (InteractiveChat.bungeecordMode) {
							try {
								BungeeMessageSender.signalPlayerDataReload(System.currentTimeMillis(), player.getUniqueId());
							} catch (Exception e) {
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
								Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> pd.save());
								sender.sendMessage(InteractiveChat.mentionEnable);
							} else {
								pd.setMentionDisabled(true);
								Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> pd.save());
								sender.sendMessage(InteractiveChat.mentionDisable);
							}
							if (InteractiveChat.bungeecordMode) {
								try {
									BungeeMessageSender.signalPlayerDataReload(System.currentTimeMillis(), player.getUniqueId());
								} catch (Exception e) {
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
		
		if (args[0].equalsIgnoreCase("setinvdisplaylayout")) {
			if (sender.hasPermission("interactivechat.module.inventory.setlayout")) {
				try {
					if (args.length == 1) {
						sender.sendMessage(InteractiveChat.notEnoughArgs);
					} else if (args.length == 2) {
						if (sender instanceof Player) {
							int layout = Integer.parseInt(args[1]);
							if (!InventoryDisplay.LAYOUTS.contains(layout)) {
								throw new NumberFormatException();
							}
							Player player = (Player) sender;
							PlayerData pd = InteractiveChat.playerDataManager.getPlayerData(player);
							pd.setInventoryDisplayLayout(layout);
							Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> pd.save());
							sender.sendMessage(InteractiveChat.setInvDisplayLayout.replace("{Layout}", layout + ""));
							if (InteractiveChat.bungeecordMode) {
								try {
									BungeeMessageSender.signalPlayerDataReload(System.currentTimeMillis(), player.getUniqueId());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} else {
							sender.sendMessage(InteractiveChat.noConsoleMessage);
						}
					} else {
						if (sender.hasPermission("interactivechat.module.inventory.setlayout.others")) {
							Player player = Bukkit.getPlayer(args[2]);
							if (player != null) {
								int layout = Integer.parseInt(args[1]);
								if (!InventoryDisplay.LAYOUTS.contains(layout)) {
									throw new NumberFormatException();
								}
								PlayerData pd = InteractiveChat.playerDataManager.getPlayerData(player);
								pd.setInventoryDisplayLayout(layout);
								Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> pd.save());
								sender.sendMessage(InteractiveChat.setInvDisplayLayout.replace("{Layout}", layout + ""));
								if (InteractiveChat.bungeecordMode) {
									try {
										BungeeMessageSender.signalPlayerDataReload(System.currentTimeMillis(), player.getUniqueId());
									} catch (Exception e) {
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
				} catch (NumberFormatException e) {
					sender.sendMessage(InteractiveChat.invalidArgs);
				}
			} else {
				sender.sendMessage(InteractiveChat.noPermissionMessage);
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("list")) {
			try {
				if (sender.hasPermission("interactivechat.list")) {
					int start = 0;
					int end = InteractiveChat.placeholderList.size();
					if (args.length > 1) {
						start = Integer.parseInt(args[1]) - 1;
						if (start < 0) {
							start = 0;
							throw new NumberFormatException();
						}
					}
					if (args.length > 2) {
						end = Integer.parseInt(args[2]);
						if (end < 0) {
							end = InteractiveChat.placeholderList.size();
							throw new NumberFormatException();
						}
					}
					InteractiveChatAPI.sendMessageUnprocessed(sender, LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.listPlaceholderHeader));
					String body = InteractiveChat.listPlaceholderBody;
					List<Component> items = new ArrayList<>();
					if (sender.hasPermission("interactivechat.list.all")) {
						int i = 0;
						for (ICPlaceholder placeholder : InteractiveChat.placeholderList) {
							i++;
							String text = body.replace("{Order}", i + "").replace("{Keyword}", placeholder.getKeyword()).replace("{Description}", placeholder.getDescription());
							items.add(LegacyComponentSerializer.legacySection().deserialize(text));
						}
					} else {
						int i = 0;
						for (ICPlaceholder placeholder : InteractiveChat.placeholderList) {
							if ((placeholder.isBuildIn() && sender.hasPermission(placeholder.getPermission())) || (!placeholder.isBuildIn() && (sender.hasPermission(placeholder.getPermission()) || !InteractiveChat.useCustomPlaceholderPermissions))) {
								i++;
								String text = body.replace("{Order}", i + "").replace("{Keyword}", placeholder.getKeyword()).replace("{Description}", placeholder.getDescription());
								items.add(LegacyComponentSerializer.legacySection().deserialize(text));
							}
						}
					}
					
					for (int i = start; i < end && i < items.size(); i++) {
						InteractiveChatAPI.sendMessageUnprocessed(sender, items.get(i));
					}
				} else {
					sender.sendMessage(InteractiveChat.noPermissionMessage);
				}
			} catch (Exception e) {
				sender.sendMessage(InteractiveChat.invalidArgs);
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
						String text = str;
						try {
							long unix = System.currentTimeMillis();
							
							if (InteractiveChat.chatAltColorCode.isPresent() && player.hasPermission("interactivechat.chatcolor.translate")) {
								text = ChatColorUtils.translateAlternateColorCodes(InteractiveChat.chatAltColorCode.get(), str);
							}
							
							Component component = Component.text(text);
							if (InteractiveChat.usePlayerName) {
								component = PlayernameDisplay.process(component, icplayer, player, unix);
					        }
					        if (InteractiveChat.useItem) {
					        	component = ItemDisplay.processWithoutCooldown(component, icplayer, player, unix);
					        }
					        if (InteractiveChat.useInventory) {
					        	component = InventoryDisplay.processWithoutCooldown(component, icplayer, player, unix);
					        }
					        if (InteractiveChat.useEnder) {
					        	component = EnderchestDisplay.processWithoutCooldown(component, icplayer, player, unix);
					        }
					        component = CustomPlaceholderDisplay.process(component, icplayer, player, InteractiveChat.placeholderList, unix, true);
					        if (InteractiveChat.clickableCommands) {
					        	component = CommandsDisplay.process(component);
					        }
					        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
						        if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.customfont.translate", true, 250)) {
						        	component = ComponentFont.parseMiniMessageFont(component);
						        }
					        }
					        
					        String json = InteractiveChatComponentSerializer.gson().serialize(component);
					        
					        if (json.length() > 32767) {
					        	InteractiveChatAPI.sendMessageUnprocessed(sender, Component.text(text));
					        } else {
					        	InteractiveChatAPI.sendMessageUnprocessed(sender, component);
					        }
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
		
		if (args[0].equalsIgnoreCase("chat")) {
			if (sender.hasPermission("interactivechat.chat")) {
				if (args.length > 1) {
					if (sender instanceof Player) {
						String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
						PacketContainer packet = InteractiveChat.protocolManager.createPacket(PacketType.Play.Client.CHAT);
						packet.getStrings().write(0, message);
						try {
							InteractiveChat.protocolManager.recieveClientPacket((Player) sender, packet);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					} else {
						sender.sendMessage(InteractiveChat.noConsoleMessage);
					}
				}
			} else {
				sender.sendMessage(InteractiveChat.noPermissionMessage);
			}
			return true;
		}
		/*
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
		*/
		if (sender instanceof Player && args.length > 1) {
			Player player = (Player) sender;
			if (args[0].equals("viewinv")) {
				PlayerData data = InteractiveChat.playerDataManager.getPlayerData(player);
				String hash = args[1];
				if (data == null || data.getInventoryDisplayLayout() == 0) {
					Inventory inv = InteractiveChat.inventoryDisplay.get(hash);
					if (inv != null) {
						Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.openInventory(inv));
					} else {
						player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.invExpiredMessage));
					}
				} else {
					Inventory inv = InteractiveChat.inventoryDisplay1Upper.get(hash);
					Inventory inv2 = InteractiveChat.inventoryDisplay1Lower.get(hash);
					if (inv != null && inv2 != null) {
						Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
							player.openInventory(inv);
							InventoryUtils.sendFakePlayerInventory(player, inv2, true, false);
							InteractiveChat.viewingInv1.put(player.getUniqueId(), hash);
						});
					} else {
						player.sendMessage(PlaceholderAPI.setPlaceholders(player, InteractiveChat.invExpiredMessage));
					}
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
		
		if (sender instanceof Player && args.length > 1 && (("chat".equalsIgnoreCase(args[0]) && sender.hasPermission("interactivechat.chat")) || ("parse".equalsIgnoreCase(args[0]) && sender.hasPermission("interactivechat.parse")))) {
			if (InteractiveChat.version.isLegacy()) {
				for (ICPlaceholder placeholder : InteractiveChat.placeholderList) {
					if (sender.hasPermission(placeholder.getPermission()) || (!placeholder.isBuildIn() && !InteractiveChat.useCustomPlaceholderPermissions)) {
						String text = placeholder.getKeyword();
						if ((placeholder.isCaseSensitive() && text.startsWith(args[args.length - 1])) || (!placeholder.isCaseSensitive() && text.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))) {
							tab.add(text);
						}
					}
				}
			} else {
				for (ICPlaceholder placeholder : InteractiveChat.placeholderList) {
					if (sender.hasPermission(placeholder.getPermission()) || (!placeholder.isBuildIn() && !InteractiveChat.useCustomPlaceholderPermissions)) {
						String text = placeholder.getKeyword();
						if ((placeholder.isCaseSensitive() && text.startsWith(args[args.length - 1])) || (!placeholder.isCaseSensitive() && text.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))) {
							Component component = LegacyComponentSerializer.legacySection().deserialize(placeholder.getDescription());
							String json = InteractiveChat.version.isLegacyRGB() ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
							tab.add(text + "\0" + json);
						}
					}
				}
			}
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
			if (sender.hasPermission("interactivechat.module.inventory.setlayout")) {
				tab.add("setinvdisplaylayout");
			}
			if (sender.hasPermission("interactivechat.chat")) {
				tab.add("chat");
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
			if (sender.hasPermission("interactivechat.module.inventory.setlayout")) {
				if ("setinvdisplaylayout".startsWith(args[0].toLowerCase())) {
					tab.add("setinvdisplaylayout");
				}
			}
			if (sender.hasPermission("interactivechat.chat")) {
				if ("chat".startsWith(args[0].toLowerCase())) {
					tab.add("chat");
				}
			}
			return tab;
		case 2:
			if (sender.hasPermission("interactivechat.mention.toggle.others")) {
				if ("mentiontoggle".equalsIgnoreCase(args[0])) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
							tab.add(player.getName());
						}
					}
				}
			}
			if (sender.hasPermission("interactivechat.module.inventory.setlayout")) {
				if ("setinvdisplaylayout".equalsIgnoreCase(args[0])) {
					for (Integer layout : InventoryDisplay.LAYOUTS) {
						tab.add(layout.toString());
					}
				}
			}
			return tab;
		case 3:
			if (sender.hasPermission("interactivechat.module.inventory.setlayout.others")) {
				if ("setinvdisplaylayout".equalsIgnoreCase(args[0])) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
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
