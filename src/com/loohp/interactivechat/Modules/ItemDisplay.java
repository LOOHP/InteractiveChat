package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.ChatColorFilter;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.ItemNBTUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MaterialUtils;
import com.loohp.interactivechat.Utils.RarityUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ItemDisplay {
	
	private static String placeholder = InteractiveChat.itemPlaceholder;
	private static boolean casesensitive = InteractiveChat.itemCaseSensitive;
	private static HashMap<Player, HashMap<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static HashMap<Player, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	private static long cooldown = ConfigManager.getConfig().getLong("ItemDisplay.Item.Cooldown") * 1000;
	
	@SuppressWarnings("deprecation")
	public static BaseComponent process(BaseComponent basecomponent, Optional<Player> optplayer, String messageKey, long unix) {
		boolean contain = (casesensitive) ? (basecomponent.toPlainText().contains(placeholder)) : (basecomponent.toPlainText().toLowerCase().contains(placeholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(placeholder) && contain) {
			if (optplayer.isPresent()) {
				Player player = optplayer.get();
				Long uc = universalCooldowns.get(player);
				if (uc != null) {
					if (uc > unix) {
						return basecomponent;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player)) {
					placeholderCooldowns.put(player, new HashMap<String, Long>());
				}
				HashMap<String, Long> spmap = placeholderCooldowns.get(player);
				if (spmap.containsKey(placeholder)) {
					if (spmap.get(placeholder) > unix) {
						if (!player.hasPermission("interactivechat.cooldown.bypass")) {
							return basecomponent;
						}
					}
				}
				spmap.put(placeholder, unix + cooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(placeholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				if (casesensitive) {
					if (!text.contains(placeholder)) {
						newlist.add(textcomponent);
						continue;
					}
				} else {
					if (!text.toLowerCase().contains(placeholder.toLowerCase())) {
						newlist.add(textcomponent);
						continue;
					}
				}
				
				String regex = casesensitive ? CustomStringUtils.escapeMetaCharacters(placeholder) : "(?i)(" + CustomStringUtils.escapeMetaCharacters(placeholder) + ")";
				List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = (TextComponent) textcomponent.duplicate();
					before.setText(trim.get(i));
					newlist.add(before);
					
					boolean endwith = casesensitive ? text.endsWith(placeholder) : text.toLowerCase().endsWith(placeholder.toLowerCase());
					if ((trim.size() - 1) > i || endwith) {
						if (trim.get(i).endsWith("\\") && !trim.get(i).endsWith("\\\\")) {
							String color = ChatColorUtils.getLastColors(newlist.get(newlist.size() - 1).toLegacyText());
							TextComponent message = new TextComponent(placeholder);
							message = (TextComponent) ChatColorUtils.applyColor(message, color);
							((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							newlist.add(message);
						} else {
							if (trim.get(i).endsWith("\\\\")) {
								((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							}
							if (optplayer.isPresent()) {
								Player player = optplayer.get();
								if (player.hasPermission("interactivechat.module.item")) {
									ItemStack item = null;							
									boolean isAir = false;
									if (InteractiveChat.version.contains("OLD")) {
										if (player.getItemInHand() == null) {
		    								isAir = true;
		    								item = new ItemStack(Material.AIR);
		    							} else if (player.getItemInHand().getType().equals(Material.AIR)) {
		    								isAir = true;
		    								item = new ItemStack(Material.AIR);
		    							} else {				            								
		    								item = player.getItemInHand();
		    							}
									} else {
										if (player.getEquipment().getItemInMainHand() == null) {
											isAir = true;
		    								item = new ItemStack(Material.AIR);
										} else if (player.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
											isAir = true;
		    								item = new ItemStack(Material.AIR);
										} else {									
											item = player.getEquipment().getItemInMainHand();
										}
									}											
								    String itemJson = ItemNBTUtils.getNMSItemStackJson(item);
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
								    itemString = ChatColorFilter.filterIllegalColorCodes(itemString);
								    amountString = String.valueOf(item.getAmount());
								    message = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.itemReplaceText.replace("{Amount}", amountString)));
								    BaseComponent[] hoverEventComponents = new BaseComponent[] {new TextComponent(itemJson)};
								    HoverEvent hoverItem = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
									String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.itemTitle));
									long time = InteractiveChat.keyTime.get(messageKey);
									if (!InteractiveChat.itemDisplay.containsKey(time)) {
										Inventory inv = Bukkit.createInventory(null, 27, title);
										ItemStack empty = InteractiveChat.itemFrame1.clone();
										if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
											empty = InteractiveChat.itemFrame2.clone();
										}
										ItemMeta emptyMeta = empty.getItemMeta();
										emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
										empty.setItemMeta(emptyMeta);
										for (int j = 0; j < inv.getSize(); j = j + 1) {
											inv.setItem(j, empty);
										}
										inv.setItem(13, item);				            							
										InteractiveChat.itemDisplay.put(time, inv);	
										HashMap<Long, Inventory> singleMap = new HashMap<Long, Inventory>();
										singleMap.put(time, inv);
									}
				            	
					            	String[] parts = message.split("\\{Item\\}");
					            	
					            	if (message.startsWith("{Item}")) {
					            		if (useTranslatable) {
											if (!InteractiveChat.version.contains("legacy")) {
												TranslatableComponent transItem = new TranslatableComponent(itemString);
												transItem.setColor(RarityUtils.getRarityColor(item));
												if (!isAir) {
													transItem.setHoverEvent(hoverItem);
												}
											    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
													ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
													transItem.setClickEvent(clickItem);
											    }
											    newlist.add(transItem);
											} else {
												BaseComponent[] itembcJson = ComponentSerializer.parse(JsonUtils.toJSON(itemString));
								            	BaseComponent itembaseJson = itembcJson[0];
												TextComponent itemitemtextcomponent = (TextComponent) itembaseJson;
												itemitemtextcomponent.setText(RarityUtils.getRarityColor(item) + itemitemtextcomponent.getText());
												itemitemtextcomponent.setColor(RarityUtils.getRarityColor(item));
												if (!isAir) {
													itemitemtextcomponent.setHoverEvent(hoverItem);
												}
											    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
													ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
													itemitemtextcomponent.setClickEvent(clickItem);
											    }
											    newlist.add(itemitemtextcomponent);
											}
										} else {
											BaseComponent[] itembcJson = ComponentSerializer.parse(JsonUtils.toJSON(itemString));
							            	BaseComponent itembaseJson = itembcJson[0];
											TextComponent itemitemtextcomponent = (TextComponent) itembaseJson;
											if (!isAir) {
												itemitemtextcomponent.setHoverEvent(hoverItem);
											}
										    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
												ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
												itemitemtextcomponent.setClickEvent(clickItem);
										    }
										    newlist.add(itemitemtextcomponent);
										}
					            	}

									for (int u = 0; u < parts.length; u++) {
										String str = parts[u];
										BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(str));
						            	BaseComponent baseJson = bcJson[0];
										TextComponent itemtextcomponent = (TextComponent) baseJson;
										if (!isAir) {
											itemtextcomponent.setHoverEvent(hoverItem);
										}
									    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
											ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
											itemtextcomponent.setClickEvent(clickItem);
									    }
									    newlist.add(itemtextcomponent);
										
										if (u < parts.length - 1 || message.endsWith("{Item}")) {
											if (useTranslatable) {
												if (!InteractiveChat.version.contains("legacy")) {
													TranslatableComponent transItem = new TranslatableComponent(itemString);
													transItem.setColor(RarityUtils.getRarityColor(item));
													if (!isAir) {
														transItem.setHoverEvent(hoverItem);
													}
												    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
														ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
														transItem.setClickEvent(clickItem);
												    }
												    newlist.add(transItem);
												} else {
													BaseComponent[] itembcJson = ComponentSerializer.parse(JsonUtils.toJSON(itemString));
									            	BaseComponent itembaseJson = itembcJson[0];
													TextComponent itemitemtextcomponent = (TextComponent) itembaseJson;
													itemitemtextcomponent.setText(RarityUtils.getRarityColor(item) + itemitemtextcomponent.getText());
													if (!isAir) {
														itemitemtextcomponent.setHoverEvent(hoverItem);
													}
												    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
														ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
														itemitemtextcomponent.setClickEvent(clickItem);
												    }
												    newlist.add(itemitemtextcomponent);
												}
											} else {
												BaseComponent[] itembcJson = ComponentSerializer.parse(JsonUtils.toJSON(itemString));
								            	BaseComponent itembaseJson = itembcJson[0];
												TextComponent itemitemtextcomponent = (TextComponent) itembaseJson;
												if (!isAir) {
													itemitemtextcomponent.setHoverEvent(hoverItem);
												}
											    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
													ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
													itemitemtextcomponent.setClickEvent(clickItem);
											    }
											    newlist.add(itemitemtextcomponent);
											}
										}
									}
								    
								} else {
									TextComponent message = new TextComponent(placeholder);
									
									newlist.add(message);
								}
							} else {
								TextComponent message = null;
								if (InteractiveChat.PlayerNotFoundReplaceEnable == true) {
									message = new TextComponent(InteractiveChat.PlayerNotFoundReplaceText.replace("{Placeholer}", placeholder));
								} else {
									message = new TextComponent(placeholder);
								}
								if (InteractiveChat.PlayerNotFoundHoverEnable == true) {
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(InteractiveChat.PlayerNotFoundHoverText.replace("{Placeholer}", placeholder)).create()));
								}
								if (InteractiveChat.PlayerNotFoundClickEnable == true) {
									String text1 = ChatColor.translateAlternateColorCodes('&', InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", placeholder));
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
		return product;
	}

}
