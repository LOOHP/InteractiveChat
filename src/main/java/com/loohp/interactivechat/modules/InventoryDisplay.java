package com.loohp.interactivechat.modules;

import java.io.IOException;
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

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent.InventoryPlaceholderType;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.LanguageUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.NBTUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.SkinUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class InventoryDisplay {
	
	public static final List<Integer> LAYOUTS = Stream.of(0, 1).collect(Collectors.toList());
	
	private static Map<UUID, Map<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static Map<UUID, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {
		String plain = PlainTextComponentSerializer.plainText().serialize(component);
		boolean contain = (InteractiveChat.invCaseSensitive) ? (plain.contains(InteractiveChat.invPlaceholder)) : (plain.toLowerCase().contains(InteractiveChat.invPlaceholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(InteractiveChat.invPlaceholder) && contain) {
			if (optplayer.isPresent()) {
				ICPlayer player = optplayer.get();
				Long uc = universalCooldowns.get(player.getUniqueId());
				if (uc != null) {
					if (uc > unix) {
						return component;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player.getUniqueId())) {
					placeholderCooldowns.put(player.getUniqueId(), new ConcurrentHashMap<String, Long>());
				}
				Map<String, Long> spmap = placeholderCooldowns.get(player.getUniqueId());
				if (spmap.containsKey(InteractiveChat.itemPlaceholder)) {
					if (spmap.get(InteractiveChat.itemPlaceholder) > unix) {
						if (!PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.cooldown.bypass", false, 5)) {
							return component;
						}
					}
				}
				spmap.put(InteractiveChat.itemPlaceholder, unix + ConfigManager.getConfig().getLong("ItemDisplay.Inventory.Cooldown") * 1000);
				InteractiveChat.universalCooldowns.put(player.getUniqueId(), unix + InteractiveChat.universalCooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(InteractiveChat.invPlaceholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		return processWithoutCooldown(component, optplayer, reciever, unix);
	}
	
	public static Component processWithoutCooldown(Component component, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {
		String plain = PlainTextComponentSerializer.plainText().serialize(component);
		boolean contain = (InteractiveChat.invCaseSensitive) ? (plain.contains(InteractiveChat.invPlaceholder)) : (plain.toLowerCase().contains(InteractiveChat.invPlaceholder.toLowerCase()));
		if (contain) {
			String regex = InteractiveChat.invCaseSensitive ? CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder) : "(?i)" + CustomStringUtils.escapeMetaCharacters(InteractiveChat.invPlaceholder);
			if (optplayer.isPresent()) {
				ICPlayer player = optplayer.get();
				if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.inventory", true, 5)) {
					
					String replaceText = InteractiveChat.invReplaceText;
					String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invTitle));
					String sha1 = HashUtils.createSha1(player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), title, player.getInventory());
					
					if (!InteractiveChat.inventoryDisplay.containsKey(sha1)) {
						layout0(player, sha1, title, reciever, component, unix);
						layout1(player, sha1, title, reciever, component, unix);
					}
					
					String componentText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));
					
					List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.Inventory.HoverMessage");
					String hoverText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, String.join("\n", hoverList)));
					
					String command = "/interactivechat viewinv " + sha1;
					
					Component invComponent = LegacyComponentSerializer.legacySection().deserialize(componentText);
					invComponent = invComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText)));
					invComponent = invComponent.clickEvent(ClickEvent.runCommand(command));
					component = ComponentReplacing.replace(component, regex, true, invComponent);
				}
			} else {
				Component message;
				if (InteractiveChat.playerNotFoundReplaceEnable) {
					message = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundReplaceText.replace("{Placeholer}", InteractiveChat.invPlaceholder));
				} else {
					message = Component.text(InteractiveChat.invPlaceholder);
				}
				if (InteractiveChat.playerNotFoundHoverEnable) {
					message = message.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundHoverText.replace("{Placeholer}", InteractiveChat.invPlaceholder))));
				}
				if (InteractiveChat.playerNotFoundClickEnable) {
					String clickValue = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholer}", InteractiveChat.invPlaceholder));
					message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(InteractiveChat.playerNotFoundClickAction), clickValue));
				}
				component = ComponentReplacing.replace(component, regex, true, message);
			}
			
			return component;
		} else {
			return component;
		}
	}
	
	public static String getLevelTranslation(int level) {
		if (level == 1) {
			return "container.enchant.level.one";
		} else {
			return "container.enchant.level.many";
		}
	}
	
	private static void layout0(ICPlayer player, String sha1, String title, Player reciever, Component component, long unix) throws Exception {
		Inventory inv = Bukkit.createInventory(null, 54, title);
		int f1 = 0;
		int f2 = 0;
		int u = 45;
		for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
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
			TranslatableComponent expText = (TranslatableComponent) Component.translatable(getLevelTranslation(level)).color(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC);
			if (level != 1) {
				expText = expText.args(Component.text(level + ""));
			}
			exp = NBTUtils.set(exp, InteractiveChatComponentSerializer.gson().serialize(expText), "display", "Name");
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
		
		InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, component, unix, inv, InventoryPlaceholderType.INVENTORY);
		Bukkit.getPluginManager().callEvent(event);
		inv = event.getInventory();
		
		Inventory finalRef = inv;
		Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
			ItemStack skull = SkinUtils.getSkull(player.getUniqueId());
			ItemMeta meta = skull.getItemMeta();
			String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invSkullName));
			meta.setDisplayName(name);
			skull.setItemMeta(meta);
			finalRef.setItem(0, skull);
		});
		
		InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY, sha1, inv);
		
		if (InteractiveChat.bungeecordMode) {
			if (player.isLocal()) {
				try {
					Inventory toForward = Bukkit.createInventory(null, 45, title);
					for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
						ItemStack item = player.getInventory().getItem(j);
						if (item != null && !item.getType().equals(Material.AIR)) {
							toForward.setItem(j, item.clone());
						}
					}
					BungeeMessageSender.forwardInventory(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, toForward);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void layout1(ICPlayer player, String sha1, String title, Player reciever, Component component, long unix) throws Exception {
		boolean rightHanded = player.isRightHanded();
		int selectedSlot = player.getSelectedSlot();
		int level = player.getExperienceLevel();
		
		Inventory inv = Bukkit.createInventory(null, 54, title);
		int f1 = 0;
		int f2 = 0;
		for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
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
			TranslatableComponent expText = (TranslatableComponent) Component.translatable(getLevelTranslation(level)).color(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC);
			if (level != 1) {
				expText = expText.args(Component.text(level + ""));
			}
			exp = NBTUtils.set(exp, InteractiveChatComponentSerializer.gson().serialize(expText), "display", "Name");
		} else {
			ItemMeta expMeta = exp.getItemMeta();
			expMeta.setDisplayName(ChatColor.YELLOW + LanguageUtils.getTranslation(getLevelTranslation(level), InteractiveChat.language).replaceFirst("%s", level + ""));
			exp.setItemMeta(expMeta);
		}
		inv.setItem(37, exp);
		
		Inventory inv2 = Bukkit.createInventory(null, 45, title);
		for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
			ItemStack item = player.getInventory().getItem(j);
			if (item != null && !item.getType().equals(Material.AIR)) {
				inv2.setItem(j, item.clone());
			}
		}
		
		InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, component, unix, inv, InventoryPlaceholderType.INVENTORY1_UPPER);
		Bukkit.getPluginManager().callEvent(event);
		inv = event.getInventory();
		
		InventoryPlaceholderEvent event2 = new InventoryPlaceholderEvent(player, reciever, component, unix, inv2, InventoryPlaceholderType.INVENTORY1_LOWER);
		Bukkit.getPluginManager().callEvent(event2);
		inv2 = event2.getInventory();
		
		Inventory finalRef = inv;
		Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
			ItemStack skull = SkinUtils.getSkull(player.getUniqueId());
			ItemMeta meta = skull.getItemMeta();
			String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invSkullName));
			meta.setDisplayName(name);
			skull.setItemMeta(meta);
			finalRef.setItem(10, skull);
		});
		
		InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY1_UPPER, sha1, inv);
		InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY1_LOWER, sha1, inv2);
		
		if (InteractiveChat.bungeecordMode) {
			if (player.isLocal()) {
				try {			    									
					BungeeMessageSender.forwardInventory(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, inv2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
