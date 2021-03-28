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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.InteractiveChatAPI;
import com.loohp.interactivechat.API.Events.InventoryPlaceholderEvent;
import com.loohp.interactivechat.API.Events.InventoryPlaceholderEvent.InventoryPlaceholderType;
import com.loohp.interactivechat.API.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.BungeeMessaging.BungeeMessageSender;
import com.loohp.interactivechat.ObjectHolders.ICPlayer;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.HashUtils;
import com.loohp.interactivechat.Utils.LanguageUtils;
import com.loohp.interactivechat.Utils.MCVersion;
import com.loohp.interactivechat.Utils.NBTUtils;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class InventoryDisplay {
	
	public static final List<Integer> LAYOUTS = Stream.of(0, 1).collect(Collectors.toList());
	
	private static Map<UUID, Map<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static Map<UUID, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	public static BaseComponent process(BaseComponent basecomponent, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {
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
		
		return processWithoutCooldown(basecomponent, optplayer, reciever, unix);
	}
	
	@SuppressWarnings("deprecation")
	public static BaseComponent processWithoutCooldown(BaseComponent basecomponent, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {		
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
				
				String regex = InteractiveChat.invCaseSensitive ? "(?<!\u00a7)" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder)) : "(?i)(?<!\u00a7)(" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder)) + ")";
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
									
									String replaceText = InteractiveChat.invReplaceText;
									String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invTitle));
									String sha1 = HashUtils.createSha1(player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), title, player.getInventory());
									
									if (!InteractiveChat.inventoryDisplay.containsKey(sha1)) {
										layout0(player, sha1, title, reciever, basecomponent, i);
										layout1(player, sha1, title, reciever, basecomponent, i);
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
		    							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewinv " + sha1));
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
	
	public static String getLevelTranslation(int level) {
		if (level == 1) {
			return "container.enchant.level.one";
		} else {
			return "container.enchant.level.many";
		}
	}
	
	private static void layout0(ICPlayer player, String sha1, String title, Player reciever, BaseComponent basecomponent, int i) throws Exception {
		Inventory inv = Bukkit.createInventory(null, 54, title);
		int f1 = 0;
		int f2 = 0;
		int u = 45;
		for (int j = 0; j < player.getInventory().getSize(); j++) {
			ItemStack item = player.getInventory().getItem(j);
			if (item != null && !item.getType().equals(Material.AIR)) {
				if ((j >= 9 && j < 18) || j >= 36) {
					if (item.getType().equals(InteractiveChat.invFrame1.getType())) {
						f1++;
					} else if (item.getType().equals(InteractiveChat.invFrame2.getType())) {
						f2++;
					}
				}
				if (j < 36) {
					inv.setItem(u, item.clone());
				}
			}
			if (u >= 53) {
				u = 18;
			} else {
				u++;
			}
		}
		ItemStack frame = f1 > f2 ? InteractiveChat.invFrame2.clone() : InteractiveChat.invFrame1.clone();
		ItemMeta frameMeta = frame.getItemMeta();
		frameMeta.setDisplayName(ChatColor.YELLOW + "");
		frame.setItemMeta(frameMeta);
		for (int j = 0; j < 18; j++) {
			inv.setItem(j, frame);
		}
		
		int level = player.getExperienceLevel();
		ItemStack exp = XMaterial.EXPERIENCE_BOTTLE.parseItem();
		if (InteractiveChat.version.isNewerThan(MCVersion.V1_15)) {
			TranslatableComponent expText = new TranslatableComponent(getLevelTranslation(level));
			if (level != 1) {
				expText.addWith(level + "");
			}
			expText.setColor(ChatColor.YELLOW);
			expText.setItalic(false);
			exp = NBTUtils.set(exp, ComponentSerializer.toString(expText), "display", "Name");
		} else {
			ItemMeta expMeta = exp.getItemMeta();
			expMeta.setDisplayName(ChatColor.YELLOW + LanguageUtils.getTranslation(getLevelTranslation(level), InteractiveChat.language).replaceFirst("%s", level + ""));
			exp.setItemMeta(expMeta);
		}
		inv.setItem(1, exp);
		
		inv.setItem(3, player.getInventory().getItem(39));
		inv.setItem(4, player.getInventory().getItem(38));
		inv.setItem(5, player.getInventory().getItem(37));
		inv.setItem(6, player.getInventory().getItem(36));
		
		ItemStack offhand = player.getInventory().getSize() > 40 ? player.getInventory().getItem(40) : null;
		if (!InteractiveChat.version.isOld() || (offhand != null && offhand.getType().equals(Material.AIR))) {
			inv.setItem(8, offhand);
		}
		
		InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, basecomponent, i, inv, InventoryPlaceholderType.INVENTORY);
		Bukkit.getPluginManager().callEvent(event);
		inv = event.getInventory();
		
		Inventory finalRef = inv;
		Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
			ItemStack skull = SkullUtils.getSkull(player.getUniqueId());
			ItemMeta meta = skull.getItemMeta();
			String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invSkullName));
			meta.setDisplayName(name);
			skull.setItemMeta(meta);
			Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> finalRef.setItem(0, skull));
		});
		
		InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY, sha1, inv);
		
		if (InteractiveChat.bungeecordMode) {
			if (player.isLocal()) {
				try {
					Inventory toForward = Bukkit.createInventory(null, 45, title);
					for (int j = 0; j < player.getInventory().getSize(); j++) {
						ItemStack item = player.getInventory().getItem(j);
						if (item != null && !item.getType().equals(Material.AIR)) {
							toForward.setItem(j, item.clone());
						}
					}
					BungeeMessageSender.forwardInventory(player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, toForward);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void layout1(ICPlayer player, String sha1, String title, Player reciever, BaseComponent basecomponent, int i) throws Exception {
		boolean rightHanded = player.isRightHanded();
		int selectedSlot = player.getSelectedSlot();
		int level = player.getExperienceLevel();
		
		Inventory inv = Bukkit.createInventory(null, 54, title);
		int f1 = 0;
		int f2 = 0;
		for (int j = 0; j < player.getInventory().getSize(); j++) {
			if (j == selectedSlot || j >= 36) {
				ItemStack item = player.getInventory().getItem(j);
				if (item != null && !item.getType().equals(Material.AIR)) {
					if (item.getType().equals(InteractiveChat.invFrame1.getType())) {
						f1++;
					} else if (item.getType().equals(InteractiveChat.invFrame2.getType())) {
						f2++;
					}
				}
			}
		}
		ItemStack frame = f1 > f2 ? InteractiveChat.invFrame2.clone() : InteractiveChat.invFrame1.clone();
		ItemMeta frameMeta = frame.getItemMeta();
		frameMeta.setDisplayName(ChatColor.YELLOW + "");
		frame.setItemMeta(frameMeta);
		for (int j = 0; j < 54; j++) {
			inv.setItem(j, frame);
		}
		inv.setItem(12, player.getInventory().getItem(39));
		inv.setItem(21, player.getInventory().getItem(38));
		inv.setItem(30, player.getInventory().getItem(37));
		inv.setItem(39, player.getInventory().getItem(36));
		
		ItemStack offhand = player.getInventory().getSize() > 40 ? player.getInventory().getItem(40) : null;
		if (InteractiveChat.version.isOld() && (offhand == null || offhand.getType().equals(Material.AIR))) {
			inv.setItem(24, player.getInventory().getItem(selectedSlot));
		} else {
			inv.setItem(23, rightHanded ? offhand : player.getInventory().getItem(selectedSlot));
			inv.setItem(25, rightHanded ? player.getInventory().getItem(selectedSlot) : offhand);
		}
		
		ItemStack exp = XMaterial.EXPERIENCE_BOTTLE.parseItem();
		if (InteractiveChat.version.isNewerThan(MCVersion.V1_15)) {
			TranslatableComponent expText = new TranslatableComponent(getLevelTranslation(level));
			if (level != 1) {
				expText.addWith(level + "");
			}
			expText.setColor(ChatColor.YELLOW);
			expText.setItalic(false);
			exp = NBTUtils.set(exp, ComponentSerializer.toString(expText), "display", "Name");
		} else {
			ItemMeta expMeta = exp.getItemMeta();
			expMeta.setDisplayName(ChatColor.YELLOW + LanguageUtils.getTranslation(getLevelTranslation(level), InteractiveChat.language).replaceFirst("%s", level + ""));
			exp.setItemMeta(expMeta);
		}
		inv.setItem(37, exp);
		
		Inventory inv2 = Bukkit.createInventory(null, 45, title);
		for (int j = 0; j < player.getInventory().getSize(); j++) {
			ItemStack item = player.getInventory().getItem(j);
			if (item != null && !item.getType().equals(Material.AIR)) {
				inv2.setItem(j, item.clone());
			}
		}
		
		InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, basecomponent, i, inv, InventoryPlaceholderType.INVENTORY1_UPPER);
		Bukkit.getPluginManager().callEvent(event);
		inv = event.getInventory();
		
		InventoryPlaceholderEvent event2 = new InventoryPlaceholderEvent(player, reciever, basecomponent, i, inv2, InventoryPlaceholderType.INVENTORY1_LOWER);
		Bukkit.getPluginManager().callEvent(event2);
		inv2 = event2.getInventory();
		
		Inventory finalRef = inv;
		Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
			ItemStack skull = SkullUtils.getSkull(player.getUniqueId());
			ItemMeta meta = skull.getItemMeta();
			String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invSkullName));
			meta.setDisplayName(name);
			skull.setItemMeta(meta);
			Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> finalRef.setItem(10, skull));
		});
		
		InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY1_UPPER, sha1, inv);
		InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY1_LOWER, sha1, inv2);
		
		if (InteractiveChat.bungeecordMode) {
			if (player.isLocal()) {
				try {			    									
					BungeeMessageSender.forwardInventory(player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, inv2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
