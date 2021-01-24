package com.loohp.interactivechat.Utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

public class FilledMapUtils {
	
	public static boolean isFilledMap(ItemStack itemStack) {
		try {
			return (itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta() instanceof MapMeta);
		} catch (Exception e) {
			return false;
		}
	}

}
