package com.loohp.interactivechat.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.ChatColor;

public class MaterialUtils {
	
	public static String getMinecraftLangName(ItemStack item) {
		
		if (InteractiveChat.version.isLegacy()) {
			return LegacyMaterialUtils.getLegacyItemName(item);
		}
		
		Material material = item.getType();
		String path = "";
		
		if (material.isBlock()) {
			path = new StringBuilder().append("block.").append(material.getKey().getNamespace()).append('.').append(material.getKey().getKey()).toString();
		} else {
			path = new StringBuilder().append("item.").append(material.getKey().getNamespace()).append('.').append(material.getKey().getKey()).toString();
		}
		
		if (item.getType().equals(Material.POTION) || item.getType().equals(Material.SPLASH_POTION) || item.getType().equals(Material.LINGERING_POTION)) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			String namespace = PotionUtils.getVanillaPotionName(meta.getBasePotionData().getType());
			path = new StringBuilder().append(path).append(".effect.").append(namespace).toString();
		}
		
		if (item.getType().equals(Material.PLAYER_HEAD)) {
			String owner = NBTUtils.getString(item, "SkullOwner", "Name");
			if (owner != null) {
				path += ".named";
			}
		}
	
		return path;
	}
	
	public static void reloadLang() {		
		if (InteractiveChat.version.isLegacy()) {
			LegacyMaterialUtils.reloadLegacyLang();
		}
	}
	
	public static void setupLang() {
		if (InteractiveChat.version.isLegacy()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Translatable Components are not supported on this version");
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] InteractiveChat will use legacy item names method instead!");
	    	LegacyMaterialUtils.setupLegacyLang();
		}
	}
}
