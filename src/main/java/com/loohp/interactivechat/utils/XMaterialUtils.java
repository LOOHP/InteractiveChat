package com.loohp.interactivechat.utils;

import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;

public class XMaterialUtils {

	@SuppressWarnings("deprecation")
	public static XMaterial matchXMaterial(ItemStack itemstack) {
		if (itemstack == null) {
			return null;
		}
		if (InteractiveChat.version.isLegacy()) {
			try {
				return XMaterial.matchXMaterial(itemstack);
			} catch (Throwable e) {
				ItemStack dataResetItemStack = itemstack.clone();
				dataResetItemStack.setDurability((short) 0);
				try {
					return XMaterial.matchXMaterial(dataResetItemStack);
				} catch (Throwable e1) {
					return null;
				}
			}
		} else {
			return XMaterial.matchXMaterial(itemstack);
		}
	}
	
	public static XMaterial matchXMaterial(String name) {
		return XMaterial.matchXMaterial(name).orElse(null);
	}
	
}
