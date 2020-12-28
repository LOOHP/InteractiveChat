package com.loohp.interactivechat.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ItemNBTUtils {
	
	private static Class<?> craftItemStackClass;
	private static Class<?> nmsItemStackClass;
	private static Method asNMSCopyMethod;
	private static Class<?> nmsNbtTagCompoundClass;
	private static Method saveNmsItemStackMethod;
	private static Constructor<?> nbtTagCompoundConstructor;
	
	public static void setup() {
		try {
			craftItemStackClass = getNMSClass("org.bukkit.craftbukkit.", "inventory.CraftItemStack");
			nmsItemStackClass = getNMSClass("net.minecraft.server.", "ItemStack");
			asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
			nmsNbtTagCompoundClass = getNMSClass("net.minecraft.server.", "NBTTagCompound");
			saveNmsItemStackMethod = nmsItemStackClass.getMethod("save", nmsNbtTagCompoundClass);
			nbtTagCompoundConstructor = nmsNbtTagCompoundClass.getConstructor();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
	
	public static String getNMSItemStackJson(ItemStack itemStack) {
	    try {
	    	Object nmsNbtTagCompoundObj = nbtTagCompoundConstructor.newInstance();
	    	Object nmsItemStackObj = asNMSCopyMethod.invoke(itemStack, itemStack);
	    	Object itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
	        return itemAsJsonObject.toString();
	    } catch (Throwable t) {
	        return "{}";
	    }
	}

}
