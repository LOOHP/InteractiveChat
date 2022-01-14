package com.loohp.interactivechat.modules;

import java.io.IOException;
import java.util.Optional;

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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.ItemPlaceholderEvent;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.hooks.ecoenchants.EcoHook;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentCompacting;
import com.loohp.interactivechat.utils.ComponentFlattening;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.FilledMapUtils;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.ItemNBTUtils;
import com.loohp.interactivechat.utils.ItemStackUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class ItemDisplay {
	
	@SuppressWarnings("deprecation")
	public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {
		String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
		if (InteractiveChat.itemPlaceholder.matcher(plain).find()) {
			String regex = InteractiveChat.itemPlaceholder.pattern();
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
					Component alternativeHover = null;
					if (!InteractiveChat.itemHover && !InteractiveChat.itemAlternativeHoverMessage.isEmpty()) {
						alternativeHover = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.itemAlternativeHoverMessage);
					}
					Component itemComponent = ComponentFlattening.flatten(createItemDisplay(player, reciever, component, unix, InteractiveChat.itemHover, alternativeHover));
					component = ComponentReplacing.replace(component, regex, true, itemComponent);
				}
			} else {
				Component message;
				if (InteractiveChat.playerNotFoundReplaceEnable) {
					message = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundReplaceText.replace("{Placeholder}", InteractiveChat.itemName));
				} else {
					message = Component.text(InteractiveChat.itemName);
				}
				if (InteractiveChat.playerNotFoundHoverEnable && InteractiveChat.itemHover) {
					message = message.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundHoverText.replace("{Placeholder}", InteractiveChat.itemName))));
				}
				if (InteractiveChat.playerNotFoundClickEnable) {
					String clickValue = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholder}", InteractiveChat.itemName));
					message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(InteractiveChat.playerNotFoundClickAction), clickValue));
				}
				component = ComponentReplacing.replace(component, regex, true, message);
			}
			
			return component;
		} else {
			return component;
		}
	}
	
	public static boolean useInventoryView(ItemStack item) {
		try {
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
		} catch (Throwable e) {}
		return false;
	}
	
	public static Component createItemDisplay(ICPlayer player, Player receiver, Component component, long timeSent) throws Exception {	
		return createItemDisplay(player, receiver, component, timeSent, true, null);
	}
	
	public static Component createItemDisplay(ICPlayer player, Player receiver, Component component, long timeSent, boolean showHover, Component alternativeHover) throws Exception {	
		ItemStack item = PlayerUtils.getHeldItem(player);
		
		if (!item.getType().equals(Material.AIR) && InteractiveChat.ecoHook) {
			item = EcoHook.setEcoLores(item, receiver);
		}
		
		ItemPlaceholderEvent event = new ItemPlaceholderEvent(player, receiver, component, timeSent, item);
		Bukkit.getPluginManager().callEvent(event);
		item = event.getItemStack();
		
		return createItemDisplay(player, item, showHover, alternativeHover);
	}
	
	public static Component createItemDisplay(ICPlayer player, ItemStack item) throws Exception {
		return createItemDisplay(player, item, true, null);
	}
	
	public static Component createItemDisplay(ICPlayer player, ItemStack item, boolean showHover, Component alternativeHover) throws Exception {
		boolean trimmed = false;
		boolean isAir = item.getType().equals(Material.AIR);
		
		ItemStack originalItem = item.clone();
		
		String itemJson = ItemNBTUtils.getNMSItemStackJson(item);
	    //Bukkit.getConsoleSender().sendMessage(itemJson.length() + "");
	    if (InteractiveChat.sendOriginalIfTooLong && itemJson.length() > InteractiveChat.itemTagMaxLength) {
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
	    Component itemDisplayNameComponent = ItemStackUtils.getDisplayName(item);
	    
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
				inv.setItem(4, isAir ? null : originalItem);
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
					inv.setItem(13, isAir ? null : originalItem);				            							
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
					inv.setItem(4, isAir ? null : originalItem);				            							
					InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
				}
			}
		}
	
		String command = isMapView ? "/interactivechat viewmap " + sha1 : "/interactivechat viewitem " + sha1;
		
		if (trimmed && InteractiveChat.cancelledMessage) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Trimmed an item display's meta data as it's NBT exceeds the maximum characters allowed in the chat [THIS IS NOT A BUG]");
		}
		
		Component itemDisplayComponent = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, item.getAmount() == 1 ? InteractiveChat.itemSingularReplaceText : InteractiveChat.itemReplaceText.replace("{Amount}", amountString))));
		itemDisplayComponent = itemDisplayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Item}").replacement(itemDisplayNameComponent).build());
		if (showHover) {
			itemDisplayComponent = itemDisplayComponent.hoverEvent(hoverEvent);
		} else if (alternativeHover != null) {
			itemDisplayComponent = itemDisplayComponent.hoverEvent(HoverEvent.showText(alternativeHover));
		}
		if (!isAir && (isMapView || (!isMapView && InteractiveChat.itemGUI))) {
			itemDisplayComponent = itemDisplayComponent.clickEvent(ClickEvent.runCommand(command));
		}
		
		return ComponentCompacting.optimize(itemDisplayComponent);
	}

}
