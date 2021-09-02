package com.loohp.interactivechat.hooks.ecoenchants;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.willfp.eco.core.display.Display;

public class EcoHook {
	
	public static ItemStack setEcoLores(ItemStack itemStack) {
		return setEcoLores(itemStack, null);
	}
	
	public static ItemStack setEcoLores(ItemStack itemStack, Player player) {
		return Display.displayAndFinalize(itemStack.clone(), player);
	}

}
