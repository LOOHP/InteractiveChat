package com.loohp.interactivechat.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.InteractiveChat;

public class ItemNBTUtils {
	
	private static Class<?> craftItemStackClass;
	private static Class<?> nmsItemStackClass;
	private static Method asNMSCopyMethod;
	private static Class<?> nmsNbtTagCompoundClass;
	private static Method saveNmsItemStackMethod;
	private static Constructor<?> nmsItemStackFromTagConstructor;
	private static Method nmsItemStackFromTagMethod;
	private static Constructor<?> nbtTagCompoundConstructor;
	private static Class<?> nmsMojangsonParserClass;
	private static Method parseMojangsonMethod;
	private static Method asBukkitCopyMethod;
	
	public static void setup() {
		try {
			craftItemStackClass = getNMSClass("org.bukkit.craftbukkit.", "inventory.CraftItemStack");
			nmsItemStackClass = getNMSClass("net.minecraft.server.", "ItemStack");
			asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
			nmsNbtTagCompoundClass = getNMSClass("net.minecraft.server.", "NBTTagCompound");
			saveNmsItemStackMethod = nmsItemStackClass.getMethod("save", nmsNbtTagCompoundClass);
			nbtTagCompoundConstructor = nmsNbtTagCompoundClass.getConstructor();
			nmsMojangsonParserClass = getNMSClass("net.minecraft.server.", "MojangsonParser");
			parseMojangsonMethod = nmsMojangsonParserClass.getMethod("parse", String.class);
			if (InteractiveChat.version.isOld()) {
				nmsItemStackFromTagMethod = nmsItemStackClass.getMethod("createStack", nmsNbtTagCompoundClass);
			} else {
				nmsItemStackFromTagConstructor = nmsItemStackClass.getDeclaredConstructor(nmsNbtTagCompoundClass);
			}
			asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {	
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
	
	public static ItemStack getItemFromNBTJson(String json) {
		try {
			Object nmsNbtTagCompoundObj = parseMojangsonMethod.invoke(null, json);
			nmsItemStackFromTagConstructor.setAccessible(true);
			Object nmsItemStackObj = InteractiveChat.version.isOld() ? nmsItemStackFromTagMethod.invoke(null, nmsNbtTagCompoundObj) : nmsItemStackFromTagConstructor.newInstance(nmsNbtTagCompoundObj);
			nmsItemStackFromTagConstructor.setAccessible(false);
			return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItemStackObj);
		} catch (Throwable e) {
			return null;
		}
	}
	
	public static String getNMSItemStackJson(ItemStack itemStack) {
	    try {
	    	Object nmsNbtTagCompoundObj = nbtTagCompoundConstructor.newInstance();
	    	Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
	    	Object itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
	        return itemAsJsonObject.toString();
	    } catch (Throwable t) {
	        return "{}";
	    }
	}

}
