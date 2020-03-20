package com.loohp.interactivechat.API;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.InteractiveChat;

public class InteractiveChatAPI {
	
	public static List<String> getPlaceholderList() {
		return InteractiveChat.placeholderList;
	}
	
	public static String getVersion() {
		return InteractiveChat.plugin.getDescription().getVersion();
	}
	
	public static long getPlayerMentionCooldown(Player player) {
		if (InteractiveChat.mentionCooldown.containsKey(player)) {
			return InteractiveChat.mentionCooldown.get(player);
		}
		return -1;
	}
	
	public static void setPlayerMentionCooldown(Player player, long time) {
		InteractiveChat.mentionCooldown.put(player, time);
	}
	
	public static HashMap<Player, Long> getMentionCooldownMap() {
		return InteractiveChat.mentionCooldown;
	}
	
	public static long getPlayerPlaceholderCooldown(Player player, String placeholder) {
		if (InteractiveChat.placeholderCooldowns.containsKey(player)) {
			if (InteractiveChat.placeholderCooldowns.get(player).containsKey(placeholder)) {
				return InteractiveChat.placeholderCooldowns.get(player).get(placeholder);
			}
		}
		return -1;
	}
	
	public static void setPlayerPlaceholderCooldown(Player player, String placeholder, long time) {
		if (!InteractiveChat.placeholderCooldowns.containsKey(player)) {
			InteractiveChat.placeholderCooldowns.put(player, new HashMap<String, Long>());
		}
		InteractiveChat.placeholderCooldowns.get(player).put(placeholder, time);
	}
	
	public static long getPlayerUniversalCooldown(Player player) {
		if (InteractiveChat.universalCooldowns.containsKey(player)) {
			return InteractiveChat.universalCooldowns.get(player);
		}
		return -1;
	}
	
	public static void setPlayerUniversalCooldown(Player player, long time) {
		InteractiveChat.universalCooldowns.put(player, time);
	}
	
	public static boolean isPlaceholderOnCooldown(Player player, String placeholder) {
		long unix = System.currentTimeMillis();
		return isPlaceholderOnCooldown(player, placeholder, unix);
	}
	
	public static boolean isPlaceholderOnCooldown(Player player, String placeholder, long time) {
		if (InteractiveChat.universalCooldowns.containsKey(player)) {
			if (InteractiveChat.universalCooldowns.get(player) > time) {
				return true;
			}
		}
		if (InteractiveChat.placeholderCooldowns.containsKey(player)) {
			if (InteractiveChat.placeholderCooldowns.get(player).containsKey(placeholder)) {
				if (InteractiveChat.placeholderCooldowns.get(player).get(placeholder) > time) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static HashMap<Long, Inventory> getInventoryShareList() {
		return InteractiveChat.inventoryDisplay;
	}
	
	public static HashMap<Long, Inventory> getEnderShareList() {
		return InteractiveChat.enderDisplay;
	}
}
