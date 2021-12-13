package com.loohp.interactivechat.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.hooks.ecoenchants.EcoHook;
import com.loohp.interactivechat.objectholders.LegacyIdKey;
import com.loohp.interactivechat.utils.ComponentCompacting;
import com.loohp.interactivechat.utils.ComponentFlattening;
import com.loohp.interactivechat.utils.FilledMapUtils;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.ItemNBTUtils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.md_5.bungee.api.ChatColor;

public class HoverableItemDisplay {
	
	@SuppressWarnings("deprecation")
	public static Component process(Component component, Player reciever) throws Exception {
		component = ComponentFlattening.flatten(component);
		List<Component> children = new ArrayList<>(component.children());
		for (int i = 0; i < children.size(); i++) {
			Component child = children.get(i);
			HoverEvent<?> hoverEvent = child.hoverEvent();
			if (hoverEvent != null && hoverEvent.action().equals(HoverEvent.Action.SHOW_ITEM) && child.clickEvent() == null) {
				ShowItem showItem = (ShowItem) hoverEvent.value();
				Key key = showItem.item();
				int count = showItem.count();
				ItemStack itemstack = null;
				LegacyIdKey legacyId = InteractiveChatComponentSerializer.interactiveChatKeyToLegacyId(key);
				if (legacyId == null) {
					String simpleNbt = "{id:\"" + key.asString() + "\", Count: " + count + "b}";
					try {
						itemstack = ItemNBTUtils.getItemFromNBTJson(simpleNbt);
					} catch (Throwable e) {}
				} else {
					Optional<XMaterial> optXMaterial = XMaterial.matchXMaterial(legacyId.getId(), legacyId.isDamageDataValue() ? (byte) legacyId.getDamage() : 0);
					if (optXMaterial.isPresent()) {
						itemstack = optXMaterial.get().parseItem();
					}
				}
				String longNbt = showItem.nbt() == null ? null : showItem.nbt().string();
				if (itemstack != null && longNbt != null) {
					try {
						itemstack = Bukkit.getUnsafe().modifyItemStack(itemstack, longNbt);
					} catch (Throwable e) {}
				}
				if (itemstack != null) {
					ClickEvent clickEvent = createItemDisplay(itemstack.clone(), reciever);
					child = child.clickEvent(clickEvent);
					children.set(i, child);
				}
			}
			if (child instanceof TranslatableComponent) {
				TranslatableComponent trans = (TranslatableComponent) child;
				List<Component> withs = new ArrayList<>(trans.args());
				for (int u = 0; u < withs.size(); u++) {
					Component with = withs.get(u);
					withs.set(u, process(with, reciever));
				}
				trans = trans.args(withs);
				children.set(i, trans);
			}
		}
		
		return ComponentCompacting.optimize(component.children(children));
	}
	
	private static ClickEvent createItemDisplay(ItemStack item, Player player) throws Exception {
		boolean isAir = item.getType().equals(Material.AIR);
		ItemStack originalItem = item.clone();
		if (!isAir && InteractiveChat.ecoHook) {
			item = EcoHook.setEcoLores(item, player);
		}
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
		
		return ClickEvent.runCommand("/interactivechat " + (isMapView ? "viewmap " : "viewitem ") + sha1);
	}

}
