package com.loohp.interactivechat.utils;

import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;

import io.github.bananapuncher714.nbteditor.NBTEditor;

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
				return XMaterial.matchXMaterial(dataResetItemStack);
			}
		} else {
			try {
				return XMaterial.matchXMaterial(itemstack);
			} catch (Throwable e) {
				ItemStack dataResetItemStack = itemstack.clone();
				if (NBTEditor.getInt(dataResetItemStack, "Damage") != 0) {
					dataResetItemStack = NBTEditor.set(dataResetItemStack, 0, "Damage");
				}
				return XMaterial.matchXMaterial(dataResetItemStack);
			}
		}
	}
	
	public static XMaterial matchXMaterial(String name) {
		return XMaterial.matchXMaterial(name).orElse(null);
	}
	
}
