package com.loohp.interactivechat.API;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;

public class InteractiveChatAPI {
	
	public static List<String> getPlaceholderList() {
		return InteractiveChat.placeholderList.stream().map(each -> each.getKeyword()).collect(Collectors.toList());
	}
	
	public static List<ICPlaceholder> getICPlaceholderList() {
		return new ArrayList<>(InteractiveChat.placeholderList);
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
		return new HashMap<>(InteractiveChat.mentionCooldown);
	}
	
	public static long getPlayerPlaceholderCooldown(Player player, String placeholder) {
		return getPlayerPlaceholderCooldown(player.getUniqueId(), placeholder);
	}
	
	public static long getPlayerPlaceholderCooldown(UUID uuid, String placeholder) {
		if (InteractiveChat.placeholderCooldowns.containsKey(uuid)) {
			if (InteractiveChat.placeholderCooldowns.get(uuid).containsKey(placeholder)) {
				return InteractiveChat.placeholderCooldowns.get(uuid).get(placeholder);
			}
		}
		return -1;
	}
	
	public static void setPlayerPlaceholderCooldown(Player player, String placeholder, long time) {
		setPlayerPlaceholderCooldown(player.getUniqueId(), placeholder, time);
	}
	
	public static void setPlayerPlaceholderCooldown(UUID uuid, String placeholder, long time) {
		if (!InteractiveChat.placeholderCooldowns.containsKey(uuid)) {
			InteractiveChat.placeholderCooldowns.put(uuid, new ConcurrentHashMap<String, Long>());
		}
		InteractiveChat.placeholderCooldowns.get(uuid).put(placeholder, time);
	}
	
	public static long getPlayerUniversalCooldown(Player player) {
		return getPlayerUniversalCooldown(player.getUniqueId());
	}
	
	public static long getPlayerUniversalCooldown(UUID uuid) {
		if (InteractiveChat.universalCooldowns.containsKey(uuid)) {
			return InteractiveChat.universalCooldowns.get(uuid);
		}
		return -1;
	}
	
	public static void setPlayerUniversalCooldown(Player player, long time) {
		setPlayerUniversalCooldown(player.getUniqueId(), time);
	}
	
	public static void setPlayerUniversalCooldown(UUID uuid, long time) {
		InteractiveChat.universalCooldowns.put(uuid, time);
	}
	
	public static boolean isPlaceholderOnCooldown(Player player, String placeholder) {
		long unix = System.currentTimeMillis();
		return isPlaceholderOnCooldown(player, placeholder, unix);
	}
	
	public static boolean isPlaceholderOnCooldown(UUID uuid, String placeholder) {
		long unix = System.currentTimeMillis();
		return isPlaceholderOnCooldown(uuid, placeholder, unix);
	}
	
	public static boolean isPlaceholderOnCooldown(Player player, String placeholder, long time) {
		return isPlaceholderOnCooldown(player.getUniqueId(), placeholder, time);
	}
	
	public static boolean isPlaceholderOnCooldown(UUID uuid, String placeholder, long time) {
		if (InteractiveChat.universalCooldowns.containsKey(uuid)) {
			if (InteractiveChat.universalCooldowns.get(uuid) > time) {
				return true;
			}
		}
		if (InteractiveChat.placeholderCooldowns.containsKey(uuid)) {
			if (InteractiveChat.placeholderCooldowns.get(uuid).containsKey(placeholder)) {
				if (InteractiveChat.placeholderCooldowns.get(uuid).get(placeholder) > time) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static BiMap<Long, Inventory> getItemShareList() {
		return HashBiMap.create(InteractiveChat.itemDisplay);
	}
	
	public static BiMap<Long, Inventory> getInventoryShareList() {
		return HashBiMap.create(InteractiveChat.inventoryDisplay);
	}
	
	public static BiMap<Long, Inventory> getEnderShareList() {
		return HashBiMap.create(InteractiveChat.enderDisplay);
	}
}
