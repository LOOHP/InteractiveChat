package com.loohp.interactivechat.API;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;

public class InteractiveChatAPI {
	
	public static List<String> getPlaceholderList() {
		return InteractiveChat.placeholderList.stream().map(each -> each.getKeyword()).collect(Collectors.toList());
	}
	
	public static List<ICPlaceholder> getICPlaceholderList() {
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
	
	public static Map<Player, Long> getMentionCooldownMap() {
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
			InteractiveChat.placeholderCooldowns.put(player, new ConcurrentHashMap<String, Long>());
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
	
	public static Map<Long, Inventory> getInventoryShareList() {
		return InteractiveChat.inventoryDisplay;
	}
	
	public static Map<Long, Inventory> getEnderShareList() {
		return InteractiveChat.enderDisplay;
	}
}
