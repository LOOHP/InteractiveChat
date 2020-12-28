package com.loohp.interactivechat.Utils;

import java.util.HashMap;

import org.bukkit.potion.PotionType;

import com.loohp.interactivechat.InteractiveChat;

public class PotionUtils {
	
	private static HashMap<PotionType, String> potionMapping = new HashMap<PotionType, String>();
	
	public static void setupPotions() {
		if (InteractiveChat.version.isLegacy()) {
			return;
		}
		
		potionMapping.put(PotionType.WATER, "water");
		potionMapping.put(PotionType.MUNDANE, "mundane");
		potionMapping.put(PotionType.THICK, "thick");
		potionMapping.put(PotionType.AWKWARD, "awkward");
		potionMapping.put(PotionType.NIGHT_VISION, "night_vision");
		potionMapping.put(PotionType.INVISIBILITY, "invisibility");
		potionMapping.put(PotionType.JUMP, "leaping");
		potionMapping.put(PotionType.FIRE_RESISTANCE, "fire_resistance");
		potionMapping.put(PotionType.SPEED, "swiftness");
		potionMapping.put(PotionType.SLOWNESS, "slowness");
		potionMapping.put(PotionType.TURTLE_MASTER, "turtle_master");
		potionMapping.put(PotionType.WATER_BREATHING, "water_breathing");
		potionMapping.put(PotionType.INSTANT_HEAL, "healing");
		potionMapping.put(PotionType.INSTANT_DAMAGE, "harming");
		potionMapping.put(PotionType.POISON, "poison");
		potionMapping.put(PotionType.REGEN, "regeneration");
		potionMapping.put(PotionType.STRENGTH, "strength");
		potionMapping.put(PotionType.WEAKNESS, "weakness");
		potionMapping.put(PotionType.LUCK, "luck");
		potionMapping.put(PotionType.SLOW_FALLING, "slow_falling");
		potionMapping.put(PotionType.UNCRAFTABLE, "empty");
	}
	
	public static String getVanillaPotionName(PotionType type) {
		if (potionMapping.containsKey(type)) {
			return potionMapping.get(type);
		}
		return potionMapping.get(PotionType.UNCRAFTABLE);
	}
}
