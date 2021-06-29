package com.loohp.interactivechat.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.ClientSettingPacket;
import com.loohp.interactivechat.objectholders.ICPlayer;

public class PlayerUtils implements Listener {
	
	private static final Map<UUID, Map<String, Boolean>> CACHE = new HashMap<>();
	private static final ItemStack AIR = new ItemStack(Material.AIR);
	
	static {
		Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
			CACHE.clear();
		}, 0, 600);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		CACHE.remove(event.getPlayer().getUniqueId());
	}
	
	public static boolean hasPermission(UUID uuid, String permission, boolean def, int timeout) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			return player.hasPermission(permission);
		} else {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
				Map<String, Boolean> map = CACHE.get(uuid);
				if (map == null) {
					map = new HashMap<>();
					CACHE.put(uuid, map);
				}
				Boolean cachedResult = map.get(permission);
				boolean result = cachedResult != null ? cachedResult : InteractiveChat.perms.playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), permission);
				future.complete(result);
				map.put(permission, result);
			});
			try {
				return future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				return def; 
			}
		}
	}

	public static ItemStack getHeldItem(Player player) {
		return getHeldItem(new ICPlayer(player));
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getHeldItem(ICPlayer player) {
		ItemStack item;							
		if (InteractiveChat.version.isOld()) {
			if (player.getEquipment().getItemInHand() == null) {
				item = AIR.clone();
			} else if (player.getEquipment().getItemInHand().getType().equals(Material.AIR)) {
				item = AIR.clone();
			} else {				            								
				item = player.getEquipment().getItemInHand().clone();
			}
		} else {
			if (player.getEquipment().getItemInMainHand() == null) {
				item = AIR.clone();
			} else if (player.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
				item = AIR.clone();
			} else {									
				item = player.getEquipment().getItemInMainHand().clone();
			}
		}
		return item;
	}
	
	public static enum ColorSettings {
		ON, 
		OFF, 
		WAITING
	}
	
	public static ColorSettings getColorSettings(Player player) {
		return ClientSettingPacket.getSettings(player);
	}
	
	public static int getProtocolVersion(Player player) {
		int protocolVersion = -1;
		if (InteractiveChat.viaVersionHook) {
			protocolVersion = us.myles.ViaVersion.api.Via.getAPI().getPlayerVersion(player.getUniqueId());
		} else if (InteractiveChat.procotcolSupportHook) {
			protocolVersion = protocolsupport.api.ProtocolSupportAPI.getProtocolVersion(player).getId();
		} else {
			protocolVersion = InteractiveChat.protocolManager.getProtocolVersion(player);
		}
		return protocolVersion;
	}

}
