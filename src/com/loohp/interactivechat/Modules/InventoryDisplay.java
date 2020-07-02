package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class InventoryDisplay {
	
	private static ConcurrentHashMap<Player, ConcurrentHashMap<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static ConcurrentHashMap<Player, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	@SuppressWarnings("deprecation")
	public static BaseComponent process(BaseComponent basecomponent, Optional<Player> optplayer, String messageKey, long unix) {
		boolean contain = (InteractiveChat.invCaseSensitive) ? (basecomponent.toPlainText().contains(InteractiveChat.invPlaceholder)) : (basecomponent.toPlainText().toLowerCase().contains(InteractiveChat.invPlaceholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(InteractiveChat.invPlaceholder) && contain) {
			if (optplayer.isPresent()) {
				Player player = optplayer.get();
				Long uc = universalCooldowns.get(player);
				if (uc != null) {
					if (uc > unix) {
						return basecomponent;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player)) {
					placeholderCooldowns.put(player, new ConcurrentHashMap<String, Long>());
				}
				ConcurrentHashMap<String, Long> spmap = placeholderCooldowns.get(player);
				if (spmap.containsKey(InteractiveChat.invPlaceholder)) {
					if (spmap.get(InteractiveChat.invPlaceholder) > unix) {
						if (!player.hasPermission("interactivechat.cooldown.bypass")) {
							return basecomponent;
						}
					}
				}
				spmap.put(InteractiveChat.invPlaceholder, unix + ConfigManager.getConfig().getLong("ItemDisplay.Inventory.Cooldown") * 1000);
			}
			InteractiveChat.cooldownbypass.get(unix).add(InteractiveChat.invPlaceholder);
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
				if (InteractiveChat.invCaseSensitive) {
					if (!text.contains(InteractiveChat.invPlaceholder)) {
						newlist.add(textcomponent);
						continue;
					}
				} else {
					if (!text.toLowerCase().contains(InteractiveChat.invPlaceholder.toLowerCase())) {
						newlist.add(textcomponent);
						continue;
					}
				}
				
				String regex = InteractiveChat.invCaseSensitive ? CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder) : "(?i)(" + CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder) + ")";
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
					
					boolean endwith = InteractiveChat.invCaseSensitive ? text.endsWith(InteractiveChat.invPlaceholder) : text.toLowerCase().endsWith(InteractiveChat.invPlaceholder.toLowerCase());
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
								Player player = optplayer.get();
								if (player.hasPermission("interactivechat.module.inventory")) {
									
									long time = InteractiveChat.keyTime.get(messageKey);
									
									String replaceText = InteractiveChat.invReplaceText;
									
									String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.invTitle));
									
									if (!InteractiveChat.inventoryDisplay.containsKey(time)) {
										Inventory inv = Bukkit.createInventory(null, 45, title);
		    							for (int j = 0; j < player.getInventory().getSize(); j = j + 1) {
		    								if (player.getInventory().getItem(j) != null) {
		    									if (!player.getInventory().getItem(j).getType().equals(Material.AIR)) {
		    										inv.setItem(j, player.getInventory().getItem(j).clone());
		    									}
		    								}
		    							}			            							
		    							InteractiveChat.inventoryDisplay.put(time, inv);	
		    							HashMap<Long, Inventory> singleMap = new HashMap<Long, Inventory>();
		    							singleMap.put(time, inv);
									}
									
									String textComp = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, replaceText));
									
									BaseComponent[] bcJson = TextComponent.fromLegacyText(textComp);
					            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
					            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
					            	
					            	List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.Inventory.HoverMessage");
									String invtext = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, String.join("\n", hoverList)));
					            	
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
								if (InteractiveChat.PlayerNotFoundReplaceEnable == true) {
									message = new TextComponent(InteractiveChat.PlayerNotFoundReplaceText.replace("{Placeholer}", InteractiveChat.invPlaceholder));
								} else {
									message = new TextComponent(InteractiveChat.invPlaceholder);
								}
								if (InteractiveChat.PlayerNotFoundHoverEnable == true) {
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(InteractiveChat.PlayerNotFoundHoverText.replace("{Placeholer}", InteractiveChat.invPlaceholder)).create()));
								}
								if (InteractiveChat.PlayerNotFoundClickEnable == true) {
									String invtext = ChatColor.translateAlternateColorCodes('&', InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", InteractiveChat.invPlaceholder));
									message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.PlayerNotFoundClickAction), invtext));
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
