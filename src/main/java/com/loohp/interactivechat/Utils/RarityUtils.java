package com.loohp.interactivechat.Utils;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.ChatColor;

public class RarityUtils {

	private static HashMap<Material, ChatColor> rarityMapping = new HashMap<Material, ChatColor>();
	
	public static void setupRarity() {
		if (InteractiveChat.version.isLegacy()) {
			return;
		}
		
		rarityMapping.put(Material.EXPERIENCE_BOTTLE, ChatColor.YELLOW);
		rarityMapping.put(Material.DRAGON_BREATH, ChatColor.YELLOW);
		rarityMapping.put(Material.ELYTRA, ChatColor.YELLOW);
		rarityMapping.put(Material.ENCHANTED_BOOK, ChatColor.YELLOW);
		rarityMapping.put(Material.ZOMBIE_HEAD, ChatColor.YELLOW);
		rarityMapping.put(Material.SKELETON_SKULL, ChatColor.YELLOW);
		rarityMapping.put(Material.CREEPER_HEAD, ChatColor.YELLOW);
		rarityMapping.put(Material.WITHER_SKELETON_SKULL, ChatColor.YELLOW);
		rarityMapping.put(Material.PLAYER_HEAD, ChatColor.YELLOW);
		rarityMapping.put(Material.HEART_OF_THE_SEA, ChatColor.YELLOW);
		rarityMapping.put(Material.NETHER_STAR, ChatColor.YELLOW);
		rarityMapping.put(Material.TOTEM_OF_UNDYING, ChatColor.YELLOW);
		rarityMapping.put(Material.BEACON, ChatColor.AQUA);
		rarityMapping.put(Material.CONDUIT, ChatColor.AQUA);
		rarityMapping.put(Material.END_CRYSTAL, ChatColor.AQUA);
		rarityMapping.put(Material.GOLDEN_APPLE, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_11, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_13, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_BLOCKS, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_CAT, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_CHIRP, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_FAR, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_MALL, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_MELLOHI, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_STAL, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_STRAD, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_WAIT, ChatColor.AQUA);
		rarityMapping.put(Material.MUSIC_DISC_WARD, ChatColor.AQUA);
		rarityMapping.put(Material.COMMAND_BLOCK, ChatColor.LIGHT_PURPLE);
		rarityMapping.put(Material.CHAIN_COMMAND_BLOCK, ChatColor.LIGHT_PURPLE);
		rarityMapping.put(Material.REPEATING_COMMAND_BLOCK, ChatColor.LIGHT_PURPLE);
		rarityMapping.put(Material.DRAGON_EGG, ChatColor.LIGHT_PURPLE);
		rarityMapping.put(Material.ENCHANTED_GOLDEN_APPLE, ChatColor.LIGHT_PURPLE);
		rarityMapping.put(Material.STRUCTURE_BLOCK, ChatColor.LIGHT_PURPLE);
		
		if (InteractiveChat.version.equals(MCVersion.V1_13) || InteractiveChat.version.equals(MCVersion.V1_13_1)) {
			return;
		}
		
		rarityMapping.put(Material.CREEPER_BANNER_PATTERN, ChatColor.YELLOW);
		rarityMapping.put(Material.SKULL_BANNER_PATTERN, ChatColor.YELLOW);
		rarityMapping.put(Material.JIGSAW, ChatColor.LIGHT_PURPLE);
		rarityMapping.put(Material.MOJANG_BANNER_PATTERN, ChatColor.LIGHT_PURPLE);
	}
	
	public static ChatColor getRarityColor(ItemStack item) {
		ChatColor color = ChatColor.WHITE;
		if (!item.getType().equals(Material.AIR)) {
			Material type = item.getType();
			if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
				color = ChatColor.AQUA;
			}
			if (rarityMapping.containsKey(type)) {
				color = rarityMapping.get(type);
			}
		}
		return color;
	}
	
	public static ChatColor getRarityColor(Material material) {
		ChatColor color = ChatColor.WHITE;
		if (!material.equals(Material.AIR)) {
			if (rarityMapping.containsKey(material)) {
				color = rarityMapping.get(material);
			}
		}
		return color;
	}
}
