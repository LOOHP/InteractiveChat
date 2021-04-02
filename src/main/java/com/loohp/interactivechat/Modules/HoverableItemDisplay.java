package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.InteractiveChatAPI;
import com.loohp.interactivechat.API.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.Utils.ChatComponentUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.FilledMapUtils;
import com.loohp.interactivechat.Utils.HashUtils;
import com.loohp.interactivechat.Utils.ItemNBTUtils;
import com.loohp.interactivechat.Utils.NBTUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Item;

public class HoverableItemDisplay {
	
	@SuppressWarnings("deprecation")
	public static BaseComponent process(BaseComponent basecomponent, Player reciever) throws Exception {
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<>();
		
		for (BaseComponent base : basecomponentlist) {
			if (base instanceof TranslatableComponent) {
				TranslatableComponent trans = (TranslatableComponent) base;
				List<BaseComponent> withs = trans.getWith();
				if (withs != null) {
					for (int i = 0; i < withs.size(); i++) {
						BaseComponent eachWith = withs.get(i);
						if (eachWith != null) {
							withs.set(i, ChatComponentUtils.cleanUpLegacyText(process(eachWith, reciever), reciever));
						}
					}
				}
			}
			if (base.getClickEvent() != null || base.getHoverEvent() == null) {
				newlist.add(base);
			} else {
				HoverEvent hoverEvent = base.getHoverEvent();
				if (hoverEvent.getAction().equals(HoverEvent.Action.SHOW_ITEM)) {
					if (InteractiveChat.legacyChatAPI) {
						BaseComponent[] components = hoverEvent.getValue();
						if (components != null) {
							for (BaseComponent forEach : components) {
								if (forEach instanceof TextComponent) {
									String text = ((TextComponent) forEach).getText();
									if (text != null) {
										ItemStack item = ItemNBTUtils.getItemFromNBTJson(text);
										if (item != null) {	
											base.setClickEvent(createItemDisplay(item));
											break;
										}
									}
								}
							}
						}
						newlist.add(base);
					} else {
						List<Content> contents = hoverEvent.getContents();
						for (Content content : contents) {
							if (content instanceof Item) {
								Item contentItem = (Item) content;
								ItemStack item = new ItemStack(Material.valueOf(contentItem.getId().replaceAll("^(.*?):", "").toUpperCase()), Math.max(1, contentItem.getCount()));
								ItemTag tag = contentItem.getTag();
								if (tag != null) {
									String nbt = tag.getNbt();
									if (nbt != null) {
										item = NBTUtils.set(item, NBTUtils.getNBTCompound(nbt));
									}
								}
								base.setClickEvent(createItemDisplay(item));
								break;
							}
						}
						newlist.add(base);
					}
				} else {
					newlist.add(base);
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
	
	private static ClickEvent createItemDisplay(ItemStack item) throws Exception {
		String title = InteractiveChat.hoverableItemTitle;
		String sha1 = HashUtils.createSha1(title, item);
		boolean isMapView = false;
		if (InteractiveChat.itemMapPreview && FilledMapUtils.isFilledMap(item)) {
			isMapView = true;
			if (!InteractiveChat.mapDisplay.containsKey(sha1)) {
				InteractiveChatAPI.addMapToMapSharedList(sha1, item);
			}
		} else if (!InteractiveChat.itemDisplay.containsKey(sha1)) {
			if (ItemDisplay.useInventoryView(item)) {
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
		
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat " + (isMapView ? "viewmap " : "viewitem ") + sha1);
	}

}
