package com.loohp.interactivechat.modules;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.ItemPlaceholderEvent;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ColorUtils;
import com.loohp.interactivechat.utils.ComponentCompacting;
import com.loohp.interactivechat.utils.ComponentFlattening;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.FilledMapUtils;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.ItemNBTUtils;
import com.loohp.interactivechat.utils.JsonUtils;
import com.loohp.interactivechat.utils.LanguageUtils;
import com.loohp.interactivechat.utils.NBTUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.RarityUtils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class ItemDisplay {
	
	private static Map<UUID, Map<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static Map<UUID, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {
		String plain = PlainComponentSerializer.plain().serialize(component);
		boolean contain = (InteractiveChat.itemCaseSensitive) ? (plain.contains(InteractiveChat.itemPlaceholder)) : (plain.toLowerCase().contains(InteractiveChat.itemPlaceholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(InteractiveChat.itemPlaceholder) && contain) {
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
				spmap.put(InteractiveChat.itemPlaceholder, unix + ConfigManager.getConfig().getLong("ItemDisplay.Item.Cooldown") * 1000);
				InteractiveChat.universalCooldowns.put(player.getUniqueId(), unix + InteractiveChat.universalCooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(InteractiveChat.itemPlaceholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		return processWithoutCooldown(component, optplayer, reciever, unix);
	}
	
	@SuppressWarnings("deprecation")
	public static Component processWithoutCooldown(Component component, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {
		String regex = InteractiveChat.itemCaseSensitive ? CustomStringUtils.escapeMetaCharacters(InteractiveChat.itemPlaceholder) : "(?i)" + CustomStringUtils.escapeMetaCharacters(InteractiveChat.itemPlaceholder);
		if (InteractiveChat.bungeecordMode && optplayer.isPresent() && optplayer.get().isLocal()) {
			ICPlayer player = optplayer.get();
			ItemStack[] equipment;
			if (InteractiveChat.version.isOld()) {
				equipment = new ItemStack[] {player.getEquipment().getHelmet(), player.getEquipment().getChestplate(), player.getEquipment().getLeggings(), player.getEquipment().getBoots(), player.getEquipment().getItemInHand()};
			} else {
				equipment = new ItemStack[] {player.getEquipment().getHelmet(), player.getEquipment().getChestplate(), player.getEquipment().getLeggings(), player.getEquipment().getBoots(), player.getEquipment().getItemInMainHand(), player.getEquipment().getItemInOffHand()};
			}
			try {
				BungeeMessageSender.forwardEquipment(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), equipment);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (optplayer.isPresent()) {
			ICPlayer player = optplayer.get();
			if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.item", true, 5)) {
				Component itemComponent = ComponentFlattening.flatten(createItemDisplay(player, reciever, component, unix));
				component = ComponentReplacing.replace(component, regex, true, itemComponent);
			}
		} else {
			Component message;
			if (InteractiveChat.playerNotFoundReplaceEnable) {
				message = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundReplaceText.replace("{Placeholer}", InteractiveChat.itemPlaceholder));
			} else {
				message = Component.text(InteractiveChat.itemPlaceholder);
			}
			if (InteractiveChat.playerNotFoundHoverEnable) {
				message = message.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundHoverText.replace("{Placeholer}", InteractiveChat.itemPlaceholder))));
			}
			if (InteractiveChat.playerNotFoundClickEnable) {
				String clickValue = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholer}", InteractiveChat.itemPlaceholder));
				message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(InteractiveChat.playerNotFoundClickAction), clickValue));
			}
			component = ComponentReplacing.replace(component, regex, true, message);
		}
		
		return component;
	}
	
	protected static boolean useInventoryView(ItemStack item) {
		if (item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta) {
			BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
			if (bsm instanceof InventoryHolder) {
				Inventory container = ((InventoryHolder) bsm).getInventory();
				if ((container.getSize() % 9) != 0) {
					return false;
				}
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
	
	@SuppressWarnings("deprecation")
	private static Component createItemDisplay(ICPlayer player, Player receiver, Component component, long timeSent) throws Exception {
		boolean trimmed = false;	
		ItemStack item;							
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
		XMaterial xMaterial = XMaterial.matchXMaterial(item);
		
		ItemPlaceholderEvent event = new ItemPlaceholderEvent(player, receiver, component, timeSent, item);
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
	    
	    //itemJson = ItemNBTUtils.convertToVersion(xMaterial, itemJson, MCVersion.V1_12);
	    //ystem.out.println(itemJson);
	    
	    String amountString = "";
	    Component itemDisplayNameComponent = null;
	    ChatColor rarityChatColor = RarityUtils.getRarityColor(item);
	    NamedTextColor rarityColor = ColorUtils.toNamedTextColor(rarityChatColor);
	    
	    String rawDisplayName = item.hasItemMeta() && item.getItemMeta() != null ? NBTUtils.getString(item, "display", "Name") : null;
	    if (rawDisplayName != null && JsonUtils.isValid(rawDisplayName)) {
	    	try {
	    		itemDisplayNameComponent = InteractiveChatComponentSerializer.gson().deserialize(rawDisplayName);
	    		if (ComponentStyling.getFirstColor(itemDisplayNameComponent) == null) {
	    			itemDisplayNameComponent = itemDisplayNameComponent.color(rarityColor);
	    		}
	    	} catch (Throwable e) {
	    		itemDisplayNameComponent = null;
	    	}
	    }
	    
	    if (itemDisplayNameComponent == null) {
		    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && !item.getItemMeta().getDisplayName().equals("")) {
		    	itemDisplayNameComponent = LegacyComponentSerializer.legacySection().deserialize(rarityChatColor + item.getItemMeta().getDisplayName());
		    } else {
		    	itemDisplayNameComponent = Component.translatable(LanguageUtils.getTranslationKey(item));
		    	itemDisplayNameComponent = itemDisplayNameComponent.color(rarityColor);
		    	if (xMaterial.equals(XMaterial.PLAYER_HEAD)) {
					String owner = NBTUtils.getString(item, "SkullOwner", "Name");
					if (owner != null) {
						itemDisplayNameComponent = ((TranslatableComponent) itemDisplayNameComponent).args(Component.text(owner));
					}
				}
		    }
	    }
	    
	    amountString = String.valueOf(item.getAmount());
	    Key key = ItemNBTUtils.getNMSItemStackNamespacedKey(item);
	    String tag = ItemNBTUtils.getNMSItemStackTag(item);
	    HoverEvent<ShowItem> hoverEvent = HoverEvent.showItem(tag == null ? ShowItem.of(key, item.getAmount()) : ShowItem.of(key, item.getAmount(), BinaryTagHolder.of(tag)));
		String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.itemTitle));
		String sha1 = HashUtils.createSha1(title, item);
		
		boolean isMapView = false;
		if (InteractiveChat.itemMapPreview && FilledMapUtils.isFilledMap(item)) {
			isMapView = true;
			if (!InteractiveChat.mapDisplay.containsKey(sha1)) {
				InteractiveChatAPI.addMapToMapSharedList(sha1, item);
			}
		} else if (!InteractiveChat.itemDisplay.containsKey(sha1)) {
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
				InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
			} else {
				if (InteractiveChat.version.isOld()) {
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
					InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
				} else {
					Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER, title);
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
					inv.setItem(4, item);				            							
					InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
				}
			}
		}
	
		String command = isMapView ? "/interactivechat viewmap " + sha1 : "/interactivechat viewitem " + sha1;
		
		if (trimmed && InteractiveChat.cancelledMessage) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Trimmed an item display's meta data as it's NBT exceeds the maximum characters allowed in the chat [THIS IS NOT A BUG]");
		}
		
		Component itemDisplayComponent = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.itemReplaceText.replace("{Amount}", amountString))));
		itemDisplayComponent = itemDisplayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Item}").replacement(itemDisplayNameComponent).build());
		itemDisplayComponent = itemDisplayComponent.hoverEvent(hoverEvent);
		if (!isAir) {
			itemDisplayComponent = itemDisplayComponent.clickEvent(ClickEvent.runCommand(command));
		}
		
		return ComponentCompacting.optimize(itemDisplayComponent, null);
	}

}
