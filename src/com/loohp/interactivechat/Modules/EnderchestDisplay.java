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

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.JsonUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class EnderchestDisplay {
	
	private static String placeholder = InteractiveChat.enderPlaceholder;
	private static boolean casesensitive = InteractiveChat.enderCaseSensitive;
	private static HashMap<Player, HashMap<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static HashMap<Player, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	private static long cooldown = ConfigManager.getConfig().getLong("ItemDisplay.EnderChest.Cooldown") * 1000;
	
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
					if (optplayer.isPresent()) {
						Player player = optplayer.get();
						if (player.hasPermission("interactivechat.module.enderchest")) {
							
							long time = InteractiveChat.keyTime.get(messageKey);
							
							String replaceText = InteractiveChat.enderReplaceText;	
							
							String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.enderTitle));
							
							if (!InteractiveChat.enderDisplay.containsKey(time)) {
								Inventory inv = Bukkit.createInventory(null, 27, title);
    							for (int j = 0; j < player.getEnderChest().getSize(); j = j + 1) {
    								if (player.getEnderChest().getItem(j) != null) {
    									if (!player.getEnderChest().getItem(j).getType().equals(Material.AIR)) {
    										inv.setItem(j, player.getEnderChest().getItem(j).clone());
    									}
    								}
    							}			            							
    							InteractiveChat.enderDisplay.put(time, inv);	
    							HashMap<Long, Inventory> singleMap = new HashMap<Long, Inventory>();
    							singleMap.put(time, inv);
							}
							
							String textComp = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, replaceText));
							
							BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(textComp));
			            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
			            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
			            	
			            	List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.EnderChest.HoverMessage");
							String endertext = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, String.join("\n", hoverList)));
			            	
			            	for (BaseComponent baseComponent : baseJson) {
			            		TextComponent message = (TextComponent) baseComponent;
			            		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(endertext).create()));
    							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewender " + time));
    							newlist.add(message);
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
							String endertext = ChatColor.translateAlternateColorCodes('&', InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", placeholder));
							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.PlayerNotFoundClickAction), endertext));
						}
						
						newlist.add(message);
					}
				}
			}
		}
		
		TextComponent product = (TextComponent) newlist.get(0);
		for (int i = 1; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return product;
	}

}
