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
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MaterialUtils;
import com.loohp.interactivechat.Utils.ItemNBTUtils;
import com.loohp.interactivechat.Utils.RarityUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
						if (player.hasPermission("interactivechat.module.item")) {
							ItemStack item = null;							
							if (InteractiveChat.version.equals("legacy1.9") || InteractiveChat.version.equals("legacy1.9.4") || InteractiveChat.version.contains("OLD")) {
								item = new ItemStack(Material.BARRIER, 1);
							} else {
								item = new ItemStack(Material.STRUCTURE_VOID, 1);
							}
							
							if (InteractiveChat.version.contains("OLD")) {
								if (player.getItemInHand() == null) {
    								ItemMeta meta = item.getItemMeta();
    								ItemStack air = new ItemStack(Material.AIR);
    								meta.setDisplayName(ChatColor.BLACK + "" + ChatColor.WHITE + MaterialUtils.getMinecraftName(air));
    								item.setItemMeta(meta);
    							} else if (player.getItemInHand().getType().equals(Material.AIR)) {
    								ItemMeta meta = item.getItemMeta();
    								ItemStack air = new ItemStack(Material.AIR);
    								meta.setDisplayName(ChatColor.BLACK + "" + ChatColor.WHITE + MaterialUtils.getMinecraftName(air));
    								item.setItemMeta(meta);
    							} else {				            								
    								item = player.getItemInHand();
    							}
							} else {
								if (player.getEquipment().getItemInMainHand() == null) {
									ItemMeta meta = item.getItemMeta();
									ItemStack air = new ItemStack(Material.AIR);
									meta.setDisplayName(ChatColor.BLACK + "" + ChatColor.WHITE + MaterialUtils.getMinecraftName(air));
									item.setItemMeta(meta);
								} else if (player.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
									ItemMeta meta = item.getItemMeta();
									ItemStack air = new ItemStack(Material.AIR);
									meta.setDisplayName(ChatColor.BLACK + "" + ChatColor.WHITE + MaterialUtils.getMinecraftName(air));
									item.setItemMeta(meta);
								} else {									
									item = player.getEquipment().getItemInMainHand();
								}
							}											
						    String itemJson = ItemNBTUtils.getNMSItemStackJson(item);
						    String message = "";
						    String itemString = "";
						    String amountString = "";
						    if (item.hasItemMeta()) {
							    if (item.getItemMeta().hasDisplayName()) {
							    	if (!item.getItemMeta().getDisplayName().equals("")) {
							    		itemString = item.getItemMeta().getDisplayName();
							    	} else {
							    		itemString = RarityUtils.getRarityColor(item) + MaterialUtils.getMinecraftName(item);
							    	}
							    } else {
							    	itemString = RarityUtils.getRarityColor(item) + MaterialUtils.getMinecraftName(item);
							    }
						    } else {
						    	itemString = RarityUtils.getRarityColor(item) + MaterialUtils.getMinecraftName(item);
						    }
						    itemString = ChatColorFilter.filterIllegalColorCodes(itemString);
						    amountString = String.valueOf(item.getAmount());
						    message = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.itemReplaceText.replace("{Item}", itemString).replace("{Amount}", amountString)));
						    BaseComponent[] hoverEventComponents = new BaseComponent[] {new TextComponent(itemJson)};
						    HoverEvent hoverItem = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
							String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.itemTitle));
							long time = InteractiveChat.keyTime.get(messageKey);
							if (!InteractiveChat.itemDisplay.containsKey(time)) {
								Inventory inv = Bukkit.createInventory(null, 27, title);
								ItemStack empty = new ItemStack(InteractiveChat.itemFrame1, 1);
								if (item.getType().equals(InteractiveChat.itemFrame1)) {
									empty = new ItemStack(InteractiveChat.itemFrame2, 1);
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
						    BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(message));
			            	BaseComponent baseJson = bcJson[0];
			            	TextComponent itemtextcomponent = (TextComponent) baseJson;
			            	itemtextcomponent.setHoverEvent(hoverItem);
						    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
								ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
								itemtextcomponent.setClickEvent(clickItem);
						    }
							newlist.add(itemtextcomponent);
						    
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
		
		TextComponent product = (TextComponent) newlist.get(0);
		for (int i = 1; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return product;
	}

}
