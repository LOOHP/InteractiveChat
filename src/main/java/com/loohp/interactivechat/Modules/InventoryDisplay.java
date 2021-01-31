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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.Events.InventoryPlaceholderEvent;
import com.loohp.interactivechat.API.Events.InventoryPlaceholderEvent.InventoryPlaceholderType;
import com.loohp.interactivechat.BungeeMessaging.BungeeMessageSender;
import com.loohp.interactivechat.ObjectHolders.ICPlayer;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class InventoryDisplay {
	
	private static Map<UUID, Map<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static Map<UUID, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	@SuppressWarnings("deprecation")
	public static BaseComponent process(BaseComponent basecomponent, Optional<ICPlayer> optplayer, Player reciever, String messageKey, long unix) {
		boolean contain = (InteractiveChat.invCaseSensitive) ? (basecomponent.toPlainText().contains(InteractiveChat.invPlaceholder)) : (basecomponent.toPlainText().toLowerCase().contains(InteractiveChat.invPlaceholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(InteractiveChat.invPlaceholder) && contain) {
			if (optplayer.isPresent()) {
				ICPlayer player = optplayer.get();
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
				spmap.put(InteractiveChat.itemPlaceholder, unix + ConfigManager.getConfig().getLong("ItemDisplay.Inventory.Cooldown") * 1000);
				InteractiveChat.universalCooldowns.put(player.getUniqueId(), unix + InteractiveChat.universalCooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(InteractiveChat.invPlaceholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				if (InteractiveChat.invCaseSensitive) {
					if (!ChatColorUtils.stripColor(text).contains(ChatColorUtils.stripColor(InteractiveChat.invPlaceholder))) {
						newlist.add(textcomponent);
						continue;
					}
				} else {
					if (!ChatColorUtils.stripColor(text).toLowerCase().contains(ChatColorUtils.stripColor(InteractiveChat.invPlaceholder).toLowerCase())) {
						newlist.add(textcomponent);
						continue;
					}
				}
				
				String regex = InteractiveChat.invCaseSensitive ? "(?<!§)" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder)) : "(?i)(?<!§)(" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder)) + ")";
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
					
					boolean endwith = InteractiveChat.invCaseSensitive ? text.matches(".*" + regex + "$") : text.toLowerCase().matches(".*" + regex.toLowerCase() + "$");
					if ((trim.size() - 1) > i || endwith) {
						if (trim.get(i).endsWith("\\") && !trim.get(i).endsWith("\\\\")) {
							String color = ChatColorUtils.getLastColors(newlist.get(newlist.size() - 1).toLegacyText());
							TextComponent message = new TextComponent(InteractiveChat.invPlaceholder);
							message = (TextComponent) ChatColorUtils.applyColor(message, color);
							((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							newlist.add(message);
						} else {
							if (trim.get(i).endsWith("\\\\")) {
								((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							}
							if (optplayer.isPresent()) {
								ICPlayer player = optplayer.get();
								if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.inventory", true, 5)) {
									
									long time = InteractiveChat.keyTime.get(messageKey);
									
									String replaceText = InteractiveChat.invReplaceText;
									
									String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invTitle));
									
									if (!InteractiveChat.inventoryDisplay.containsKey(time)) {
										Inventory inv = Bukkit.createInventory(null, 45, title);
		    							for (int j = 0; j < player.getInventory().getSize(); j = j + 1) {
		    								if (player.getInventory().getItem(j) != null) {
		    									if (!player.getInventory().getItem(j).getType().equals(Material.AIR)) {
		    										inv.setItem(j, player.getInventory().getItem(j).clone());
		    									}
		    								}
		    							}
		    							
		    							InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, basecomponent, i, inv, InventoryPlaceholderType.INVENTORY);
										Bukkit.getPluginManager().callEvent(event);
										inv = event.getInventory();
		    							
		    							InteractiveChat.inventoryDisplay.put(time, inv);	
		    							if (InteractiveChat.bungeecordMode) {
			    							if (player.isLocal()) {
			    								try {
													BungeeMessageSender.forwardInventory(player.getUniqueId(), null, inv);
												} catch (IOException e) {
													e.printStackTrace();
												}
			    							}
		    							}
									}
									
									String textComp = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));
									
									BaseComponent[] bcJson = TextComponent.fromLegacyText(textComp);
					            	List<BaseComponent> baseJson = new ArrayList<>();
					            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
					            	
					            	List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.Inventory.HoverMessage");
									String invtext = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, String.join("\n", hoverList)));
					            	
					            	for (BaseComponent baseComponent : baseJson) {
					            		TextComponent message = (TextComponent) baseComponent;
					            		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(invtext).create()));
		    							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewinv " + time));
		    							newlist.add(message);
					            	}
														            							
								} else {
									TextComponent message = new TextComponent(InteractiveChat.invPlaceholder);
									
									newlist.add(message);
								}
							} else {
								TextComponent message = null;
								if (InteractiveChat.playerNotFoundReplaceEnable == true) {
									message = new TextComponent(InteractiveChat.playerNotFoundReplaceText.replace("{Placeholer}", InteractiveChat.invPlaceholder));
								} else {
									message = new TextComponent(InteractiveChat.invPlaceholder);
								}
								if (InteractiveChat.playerNotFoundHoverEnable == true) {
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(InteractiveChat.playerNotFoundHoverText.replace("{Placeholer}", InteractiveChat.invPlaceholder)).create()));
								}
								if (InteractiveChat.playerNotFoundClickEnable == true) {
									String invtext = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholer}", InteractiveChat.invPlaceholder));
									message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.playerNotFoundClickAction), invtext));
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
