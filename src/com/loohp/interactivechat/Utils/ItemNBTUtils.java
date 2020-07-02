package com.loohp.interactivechat.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ItemNBTUtils {
	
	private static Class<?> craftItemStackClass;
	private static Class<?> nmsItemStackClass;
	private static MethodHandle asNMSCopyMethod;
	private static Class<?> nmsNbtTagCompoundClass;
	private static MethodHandle saveNmsItemStackMethod;
	private static MethodHandle nbtTagCompoundConstructor;
	
	public static void setup() {
		try {
			craftItemStackClass = getNMSClass("org.bukkit.craftbukkit.", "inventory.CraftItemStack");
			nmsItemStackClass = getNMSClass("net.minecraft.server.", "ItemStack");
			asNMSCopyMethod = MethodHandles.lookup().findStatic(craftItemStackClass, "asNMSCopy", MethodType.methodType(nmsItemStackClass, ItemStack.class));
			nmsNbtTagCompoundClass = getNMSClass("net.minecraft.server.", "NBTTagCompound");
			saveNmsItemStackMethod = MethodHandles.lookup().findVirtual(nmsItemStackClass, "save", MethodType.methodType(nmsNbtTagCompoundClass, nmsNbtTagCompoundClass));
			nbtTagCompoundConstructor = MethodHandles.lookup().findConstructor(nmsNbtTagCompoundClass, MethodType.methodType(void.class));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
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
	    	Object nmsNbtTagCompoundObj = nbtTagCompoundConstructor.invoke();
	    	Object nmsItemStackObj = asNMSCopyMethod.invoke(itemStack);
	    	Object itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
	        return itemAsJsonObject.toString();
	    } catch (Throwable t) {
	        return "{}";
	    }
	}

}
