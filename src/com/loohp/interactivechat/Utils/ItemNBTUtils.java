package com.loohp.interactivechat.Utils;

import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.NMS.ReflectionUtil;

public class ItemNBTUtils {
	
	public static String getNMSItemStackJson(ItemStack itemStack) {
		
	    Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
	    Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
	    
	    Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
	    Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
	    Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

	    Object nmsNbtTagCompoundObj;
	    Object nmsItemStackObj;
	    Object itemAsJsonObject;

	    try {
	        nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
	        nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
	        itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
	    } catch (Throwable t) {
	        return "{}";
	    }

	    return itemAsJsonObject.toString();
	}

}
