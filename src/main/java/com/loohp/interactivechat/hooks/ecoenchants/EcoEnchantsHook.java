package com.loohp.interactivechat.hooks.ecoenchants;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EcoEnchantsHook {
	
	private static Method ecoEnchantsPluginGetInstanceMethod;
	private static Method ecoEnchantsPluginGetDisplayModuleMethod;
	private static Method displayModuleDisplayMethod;
	private static Method displayModuleGenerateVarArgsMethod;
	
	static {
		try {
			Class<?> ecoEnchantsPluginClass = Class.forName("com.willfp.ecoenchants.EcoEnchantsPlugin");
			ecoEnchantsPluginGetInstanceMethod = ecoEnchantsPluginClass.getMethod("getInstance");
			ecoEnchantsPluginGetDisplayModuleMethod = ecoEnchantsPluginClass.getMethod("getDisplayModule");
			Class<?> enchantDisplayClass = Class.forName("com.willfp.ecoenchants.display.EnchantDisplay");
			displayModuleDisplayMethod = enchantDisplayClass.getDeclaredMethod("display", ItemStack.class, Player.class, Object[].class);
			displayModuleGenerateVarArgsMethod = enchantDisplayClass.getDeclaredMethod("generateVarArgs", ItemStack.class);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static ItemStack setEcoEnchantLores(ItemStack itemstack) {
		try {
			Object displayModule = ecoEnchantsPluginGetDisplayModuleMethod.invoke(ecoEnchantsPluginGetInstanceMethod.invoke(Bukkit.getPluginManager().getPlugin("EcoEnchants")));
			displayModuleGenerateVarArgsMethod.setAccessible(true);
			Object[] varArgs = (Object[]) displayModuleGenerateVarArgsMethod.invoke(displayModule, itemstack);
			displayModuleDisplayMethod.setAccessible(true);
			displayModuleDisplayMethod.invoke(displayModule, itemstack, null, varArgs);
			return itemstack.clone();
		} catch (Exception e) {
			e.printStackTrace();
			return itemstack.clone();
		}
	}

}
