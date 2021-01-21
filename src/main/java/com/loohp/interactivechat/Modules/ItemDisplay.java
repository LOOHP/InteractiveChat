package com.loohp.interactivechat.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.Events.ItemPlaceholderEvent;
import com.loohp.interactivechat.BungeeMessaging.BungeeMessageSender;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.ItemNBTUtils;
import com.loohp.interactivechat.Utils.MaterialUtils;
import com.loohp.interactivechat.Utils.NBTUtils;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.PlayerUtils;
import com.loohp.interactivechat.Utils.RarityUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class ItemDisplay {
	
	private static Map<UUID, Map<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static Map<UUID, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	@SuppressWarnings("deprecation")
	public static BaseComponent process(BaseComponent basecomponent, Optional<PlayerWrapper> optplayer, Player reciever, String messageKey, long unix) {
		boolean contain = (InteractiveChat.itemCaseSensitive) ? (basecomponent.toPlainText().contains(InteractiveChat.itemPlaceholder)) : (basecomponent.toPlainText().toLowerCase().contains(InteractiveChat.itemPlaceholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(InteractiveChat.itemPlaceholder) && contain) {
			if (optplayer.isPresent()) {
				PlayerWrapper player = optplayer.get();
				Long uc = universalCooldowns.get(player.getUniqueId());
				if (uc != null) {
					if (uc > unix) {
						return basecomponent;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player.getUniqueId())) {
					placeholderCooldowns.put(player.getUniqueId(), new ConcurrentHashMap<String, Long>());
				}
				Map<String, Long> spmap = placeholderCooldowns.get(player.getUniqueId());
				if (spmap.containsKey(InteractiveChat.itemPlaceholder)) {
					if (spmap.get(InteractiveChat.itemPlaceholder) > unix) {
						if (!PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.cooldown.bypass", false, 5)) {
							return basecomponent;
						}
					}
				}
				spmap.put(InteractiveChat.itemPlaceholder, unix + ConfigManager.getConfig().getLong("ItemDisplay.Item.Cooldown") * 1000);
				InteractiveChat.universalCooldowns.put(player.getUniqueId(), unix + InteractiveChat.universalCooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(InteractiveChat.itemPlaceholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		if (InteractiveChat.bungeecordMode && optplayer.isPresent() && optplayer.get().isLocal()) {
			PlayerWrapper player = optplayer.get();
			ItemStack[] equipment;
			if (InteractiveChat.version.isOld()) {
				equipment = new ItemStack[] {player.getEquipment().getHelmet(), player.getEquipment().getChestplate(), player.getEquipment().getLeggings(), player.getEquipment().getBoots(), player.getEquipment().getItemInHand()};
			} else {
				equipment = new ItemStack[] {player.getEquipment().getHelmet(), player.getEquipment().getChestplate(), player.getEquipment().getLeggings(), player.getEquipment().getBoots(), player.getEquipment().getItemInMainHand(), player.getEquipment().getItemInOffHand()};
			}
			try {
				BungeeMessageSender.forwardEquipment(player.getUniqueId(), equipment);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		boolean trimmed = false;
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				if (InteractiveChat.itemCaseSensitive) {
					if (!ChatColorUtils.stripColor(text).contains(ChatColorUtils.stripColor(InteractiveChat.itemPlaceholder))) {
						newlist.add(textcomponent);
						continue;
					}
				} else {
					if (!ChatColorUtils.stripColor(text).toLowerCase().contains(ChatColorUtils.stripColor(InteractiveChat.itemPlaceholder).toLowerCase())) {
						newlist.add(textcomponent);
						continue;
					}
				}
				
				String regex = InteractiveChat.itemCaseSensitive ? "(?<!§)" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(InteractiveChat.itemPlaceholder)) : "(?i)(?<!§)(" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(InteractiveChat.itemPlaceholder)) + ")";
				List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}
				
				String lastColor = "";
				
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = new TextComponent(textcomponent);
					before.setText(lastColor + trim.get(i));
					newlist.add(before);
					lastColor = ChatColorUtils.getLastColors(before.getText());
					
					boolean endwith = InteractiveChat.itemCaseSensitive ? text.matches(".*" + regex + "$") : text.toLowerCase().matches(".*" + regex.toLowerCase() + "$");
					if ((trim.size() - 1) > i || endwith) {
						if (trim.get(i).endsWith("\\") && !trim.get(i).endsWith("\\\\")) {
							String color = ChatColorUtils.getLastColors(newlist.get(newlist.size() - 1).toLegacyText());
							TextComponent message = new TextComponent(InteractiveChat.itemPlaceholder);
							message = (TextComponent) ChatColorUtils.applyColor(message, color);
							((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							newlist.add(message);
						} else {
							if (trim.get(i).endsWith("\\\\")) {
								((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							}
							if (optplayer.isPresent()) {
								PlayerWrapper player = optplayer.get();
								if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.item", true, 5)) {
									ItemStack item = null;							
									boolean isAir = false;
									if (InteractiveChat.version.isOld()) {
										if (player.getEquipment().getItemInHand() == null) {
		    								isAir = true;
		    								item = new ItemStack(Material.AIR);
		    							} else if (player.getEquipment().getItemInHand().getType().equals(Material.AIR)) {
		    								isAir = true;
		    								item = new ItemStack(Material.AIR);
		    							} else {				            								
		    								item = player.getEquipment().getItemInHand().clone();
		    							}
									} else {
										if (player.getEquipment().getItemInMainHand() == null) {
											isAir = true;
		    								item = new ItemStack(Material.AIR);
										} else if (player.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
											isAir = true;
		    								item = new ItemStack(Material.AIR);
										} else {									
											item = player.getEquipment().getItemInMainHand().clone();
										}
									}
									
									ItemPlaceholderEvent event = new ItemPlaceholderEvent(player, reciever, basecomponent, i, item);
									Bukkit.getPluginManager().callEvent(event);
									item = event.getItemStack();
									
								    String itemJson = ItemNBTUtils.getNMSItemStackJson(item);
								    //Bukkit.getConsoleSender().sendMessage(itemJson.length() + "");
								    if (InteractiveChat.sendOriginalIfTooLong && itemJson.length() > 32767) {
								    	ItemStack trimedItem = new ItemStack(item.getType());
								    	trimedItem.addUnsafeEnchantments(item.getEnchantments());
								    	if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
								    		ItemStack loreItem = trimedItem.clone();
							    			ItemMeta meta = loreItem.getItemMeta();
							    			meta.setLore(item.getItemMeta().getLore());
							    			loreItem.setItemMeta(meta);
							    			String newjson = ItemNBTUtils.getNMSItemStackJson(loreItem);
							    			if (newjson.length() <= 30000) {
							    				trimedItem = loreItem;
							    			}
								    	}
								    	itemJson = ItemNBTUtils.getNMSItemStackJson(trimedItem);
								    	trimmed = true;
								    }
								    String message = "";
								    String itemString = "";
								    String amountString = "";
								    boolean useTranslatable = false;
								    if (item.hasItemMeta()) {
									    if (item.getItemMeta().hasDisplayName()) {
									    	if (!item.getItemMeta().getDisplayName().equals("")) {
									    		itemString = item.getItemMeta().getDisplayName();
									    	} else {
									    		useTranslatable = true;
									    		itemString = MaterialUtils.getMinecraftLangName(item);
									    	}
									    } else {
									    	useTranslatable = true;
									    	itemString = MaterialUtils.getMinecraftLangName(item);
									    }
								    } else {
								    	useTranslatable = true;
								    	itemString = MaterialUtils.getMinecraftLangName(item);
								    }							
								    itemString = ChatColorUtils.filterIllegalColorCodes(itemString);
								    amountString = String.valueOf(item.getAmount());
								    message = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.itemReplaceText.replace("{Amount}", amountString)));
								    BaseComponent[] hoverEventComponents = new BaseComponent[] {new TextComponent(itemJson)};
								    HoverEvent hoverItem = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
									String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.itemTitle));
									long time = InteractiveChat.keyTime.get(messageKey);
									if (!InteractiveChat.itemDisplay.containsKey(time)) {
										if (useInventoryView(item)) {
											Inventory container = ((InventoryHolder) ((BlockStateMeta) item.getItemMeta()).getBlockState()).getInventory();
											Inventory inv = Bukkit.createInventory(null, container.getSize() + 9, title);
											ItemStack empty = InteractiveChat.itemFrame1.clone();
											if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
												empty = InteractiveChat.itemFrame2.clone();
											}
											ItemMeta emptyMeta = empty.getItemMeta();
											emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
											empty.setItemMeta(emptyMeta);
											for (int j = 0; j < 9; j++) {
												inv.setItem(j, empty);
											}
											inv.setItem(4, item);
											for (int j = 0; j < container.getSize(); j++) {
												ItemStack shulkerItem = container.getItem(j);
												if (shulkerItem != null && !shulkerItem.getType().equals(Material.AIR)) {
													inv.setItem(j + 9, shulkerItem == null ? null : shulkerItem.clone());
												}
											}										
											InteractiveChat.itemDisplay.put(time, inv);	
										} else {
											Inventory inv = Bukkit.createInventory(null, 27, title);
											ItemStack empty = InteractiveChat.itemFrame1.clone();
											if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
												empty = InteractiveChat.itemFrame2.clone();
											}
											ItemMeta emptyMeta = empty.getItemMeta();
											emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
											empty.setItemMeta(emptyMeta);
											for (int j = 0; j < inv.getSize(); j++) {
												inv.setItem(j, empty);
											}
											inv.setItem(13, item);				            							
											InteractiveChat.itemDisplay.put(time, inv);	
										}
									}
				            	
					            	String[] parts = message.split("\\{Item\\}");					            	
					            	if (message.startsWith("{Item}")) {
					            		if (useTranslatable) {
											if (!InteractiveChat.version.isLegacy()) {
												TranslatableComponent transItem = new TranslatableComponent(itemString);
												if (item.getType().equals(Material.PLAYER_HEAD)) {
													String owner = NBTUtils.getString(item, "SkullOwner", "Name");
													if (owner != null) {
														transItem.addWith(owner);
													}
												}
												transItem.setColor(RarityUtils.getRarityColor(item));
												if (!isAir) {
													transItem.setHoverEvent(hoverItem);
												}
											    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled")) {
													ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
													transItem.setClickEvent(clickItem);
											    }
											    newlist.add(transItem);
											} else {
												TextComponent itemitemtextcomponent = new TextComponent(itemString);
												itemitemtextcomponent.setText(RarityUtils.getRarityColor(item) + itemitemtextcomponent.getText());
												if (!isAir) {
													itemitemtextcomponent.setHoverEvent(hoverItem);
												}
											    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled")) {
													ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
													itemitemtextcomponent.setClickEvent(clickItem);
											    }
											    newlist.add(itemitemtextcomponent);
											}
										} else {
											TextComponent itemitemtextcomponent = new TextComponent(itemString);
											if (!isAir) {
												itemitemtextcomponent.setHoverEvent(hoverItem);
											}
										    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled")) {
												ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
												itemitemtextcomponent.setClickEvent(clickItem);
										    }
										    newlist.add(itemitemtextcomponent);
										}
					            	}

									for (int u = 0; u < parts.length; u++) {
										String str = parts[u];
										TextComponent itemtextcomponent = new TextComponent(str);
										if (!isAir) {
											itemtextcomponent.setHoverEvent(hoverItem);
										}
									    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled")) {
											ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
											itemtextcomponent.setClickEvent(clickItem);
									    }
									    newlist.add(itemtextcomponent);
										
										if (u < parts.length - 1 || message.endsWith("{Item}")) {
											if (useTranslatable) {
												if (!InteractiveChat.version.isLegacy()) {
													TranslatableComponent transItem = new TranslatableComponent(itemString);
													transItem.setColor(RarityUtils.getRarityColor(item));
													if (item.getType().equals(Material.PLAYER_HEAD)) {
														String owner = NBTUtils.getString(item, "SkullOwner", "Name");
														if (owner != null) {
															transItem.addWith(owner);
														}
													}
													if (!isAir) {
														transItem.setHoverEvent(hoverItem);
													}
												    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled")) {
														ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
														transItem.setClickEvent(clickItem);
												    }
												    newlist.add(transItem);
												} else {
													TextComponent itemitemtextcomponent = new TextComponent(itemString);
													itemitemtextcomponent.setText(RarityUtils.getRarityColor(item) + itemitemtextcomponent.getText());
													if (!isAir) {
														itemitemtextcomponent.setHoverEvent(hoverItem);
													}
												    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled")) {
														ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
														itemitemtextcomponent.setClickEvent(clickItem);
												    }
												    newlist.add(itemitemtextcomponent);
												}
											} else {
												TextComponent itemitemtextcomponent = new TextComponent(itemString);
												if (!isAir) {
													itemitemtextcomponent.setHoverEvent(hoverItem);
												}
											    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled")) {
													ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
													itemitemtextcomponent.setClickEvent(clickItem);
											    }
											    newlist.add(itemitemtextcomponent);
											}
										}
									}
								} else {
									TextComponent message = new TextComponent(InteractiveChat.itemPlaceholder);
									
									newlist.add(message);
								}
							} else {
								TextComponent message = null;
								if (InteractiveChat.PlayerNotFoundReplaceEnable) {
									message = new TextComponent(InteractiveChat.PlayerNotFoundReplaceText.replace("{Placeholer}", InteractiveChat.itemPlaceholder));
								} else {
									message = new TextComponent(InteractiveChat.itemPlaceholder);
								}
								if (InteractiveChat.PlayerNotFoundHoverEnable) {
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(InteractiveChat.PlayerNotFoundHoverText.replace("{Placeholer}", InteractiveChat.itemPlaceholder)).create()));
								}
								if (InteractiveChat.PlayerNotFoundClickEnable) {
									String text1 = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", InteractiveChat.itemPlaceholder));
									message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.PlayerNotFoundClickAction), text1));
								}
								
								newlist.add(message);
							}
						}
					}
				}
			}
		}
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		
		if (trimmed && InteractiveChat.cancelledMessage) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Trimmed an item display's meta data as it's NBT exceeds the maximum characters allowed in the chat [THIS IS NOT A BUG]");
		}
		
		return product;
	}
	
	private static boolean useInventoryView(ItemStack item) {
		if (item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta) {
			BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
			if (bsm instanceof InventoryHolder) {
				Inventory container = ((InventoryHolder) bsm).getInventory();
				for (int i = 0; i < container.getSize(); i++) {
					ItemStack containerItem = container.getItem(i);
					if (containerItem != null && !containerItem.getType().equals(Material.AIR)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
