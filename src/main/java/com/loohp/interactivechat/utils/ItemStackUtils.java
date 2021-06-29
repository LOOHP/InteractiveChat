package com.loohp.interactivechat.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.InteractiveChat;

public class ItemStackUtils {
	
	private static Class<?> craftItemStackClass;
	private static Class<?> nmsItemStackClass;
	private static Method asBukkitCopyMethod;
	private static Method asNMSCopyMethod;
	
	static {
		try {
			craftItemStackClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
			nmsItemStackClass = NMSUtils.getNMSClass("net.minecraft.server.%s.ItemStack", "net.minecraft.world.item.ItemStack");
			asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
			asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
		} catch (ClassNotFoundException | SecurityException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public static boolean isArmor(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		String typeNameString = itemStack.getType().name();
		return typeNameString.endsWith("_HELMET") || typeNameString.endsWith("_CHESTPLATE") || typeNameString.endsWith("_LEGGINGS") || typeNameString.endsWith("_BOOTS");
	}
	
	public static boolean isWearable(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		if (isArmor(itemStack)) {
			return true;
		}
		String typeNameString = itemStack.getType().name();
		if (typeNameString.equals("ELYTRA")) {
			return true;
		}
		if (typeNameString.contains("HEAD") || typeNameString.contains("SKULL")) {
			return true;
		}
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_13)) {
			return typeNameString.equals("CARVED_PUMPKIN");
		} else {
			return typeNameString.equals("PUMPKIN");
		}
	}
	
	public static ItemStack toBukkitCopy(Object handle) {
		try {
			return (ItemStack) asBukkitCopyMethod.invoke(null, handle);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}
	
	public static Object toNMSCopy(ItemStack itemstack) {
		try {
			return asNMSCopyMethod.invoke(null, itemstack);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}

}
