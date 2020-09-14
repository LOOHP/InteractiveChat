package com.loohp.interactivechat.Modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.PluginMessaging.BungeeMessageSender;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class EnderchestDisplay {
	
	private static ConcurrentHashMap<Player, ConcurrentHashMap<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static ConcurrentHashMap<Player, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	@SuppressWarnings("deprecation")
	public static BaseComponent process(BaseComponent basecomponent, Optional<PlayerWrapper> optplayer, String messageKey, long unix) {
		boolean contain = (InteractiveChat.enderCaseSensitive) ? (basecomponent.toPlainText().contains(InteractiveChat.enderPlaceholder)) : (basecomponent.toPlainText().toLowerCase().contains(InteractiveChat.enderPlaceholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(InteractiveChat.enderPlaceholder) && contain) {
			if (optplayer.isPresent() && optplayer.get().isLocal()) {
				Player player = optplayer.get().getLocalPlayer();
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
				if (spmap.containsKey(InteractiveChat.enderPlaceholder)) {
					if (spmap.get(InteractiveChat.enderPlaceholder) > unix) {
						if (!player.hasPermission("interactivechat.cooldown.bypass")) {
							return basecomponent;
						}
					}
				}
				spmap.put(InteractiveChat.enderPlaceholder, unix + ConfigManager.getConfig().getLong("ItemDisplay.EnderChest.Cooldown") * 1000);
			}
			InteractiveChat.cooldownbypass.get(unix).add(InteractiveChat.enderPlaceholder);
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
				if (InteractiveChat.enderCaseSensitive) {
					if (!text.contains(InteractiveChat.enderPlaceholder)) {
						newlist.add(textcomponent);
						continue;
					}
				} else {
					if (!text.toLowerCase().contains(InteractiveChat.enderPlaceholder.toLowerCase())) {
						newlist.add(textcomponent);
						continue;
					}
				}
				
				String regex = InteractiveChat.enderCaseSensitive ? CustomStringUtils.escapeMetaCharacters(InteractiveChat.enderPlaceholder) : "(?i)(" + CustomStringUtils.escapeMetaCharacters(InteractiveChat.enderPlaceholder) + ")";
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
					
					boolean endwith = InteractiveChat.enderCaseSensitive ? text.endsWith(InteractiveChat.enderPlaceholder) : text.toLowerCase().endsWith(InteractiveChat.enderPlaceholder.toLowerCase());
					if ((trim.size() - 1) > i || endwith) {
						if (trim.get(i).endsWith("\\") && !trim.get(i).endsWith("\\\\")) {
							String color = ChatColorUtils.getLastColors(newlist.get(newlist.size() - 1).toLegacyText());
							TextComponent message = new TextComponent(InteractiveChat.enderPlaceholder);
							message = (TextComponent) ChatColorUtils.applyColor(message, color);
							((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							newlist.add(message);
						} else {
							if (trim.get(i).endsWith("\\\\")) {
								((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							}
							if (optplayer.isPresent()) {
								PlayerWrapper player = optplayer.get();
								if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.enderchest", true, 5)) {
									
									long time = InteractiveChat.keyTime.get(messageKey);
									
									String replaceText = InteractiveChat.enderReplaceText;	
									
									String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.enderTitle));
									
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
		    							if (InteractiveChat.bungeecordMode) {
			    							if (player.isLocal()) {
			    								try {
													BungeeMessageSender.forwardEnderchest(player.getUniqueId(), null, inv);
												} catch (IOException e) {
													e.printStackTrace();
												}
			    							}
		    							}
									}
									
									String textComp = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));
									
									BaseComponent[] bcJson = TextComponent.fromLegacyText(textComp);
					            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
					            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
					            	
					            	List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.EnderChest.HoverMessage");
									String endertext = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, String.join("\n", hoverList)));
					            	
					            	for (BaseComponent baseComponent : baseJson) {
					            		TextComponent message = (TextComponent) baseComponent;
					            		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(endertext).create()));
		    							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewender " + time));
		    							newlist.add(message);
					            	}
								} else {
									TextComponent message = new TextComponent(InteractiveChat.enderPlaceholder);
									
									newlist.add(message);
								}
							} else {
								TextComponent message = null;
								if (InteractiveChat.PlayerNotFoundReplaceEnable == true) {
									message = new TextComponent(InteractiveChat.PlayerNotFoundReplaceText.replace("{Placeholer}", InteractiveChat.enderPlaceholder));
								} else {
									message = new TextComponent(InteractiveChat.enderPlaceholder);
								}
								if (InteractiveChat.PlayerNotFoundHoverEnable == true) {
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(InteractiveChat.PlayerNotFoundHoverText.replace("{Placeholer}", InteractiveChat.enderPlaceholder)).create()));
								}
								if (InteractiveChat.PlayerNotFoundClickEnable == true) {
									String endertext = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", InteractiveChat.enderPlaceholder));
									message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.PlayerNotFoundClickAction), endertext));
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
