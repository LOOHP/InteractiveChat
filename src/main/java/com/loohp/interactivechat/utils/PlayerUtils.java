package com.loohp.interactivechat.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.PermissionCache;

public class PlayerUtils implements Listener {
	
	private static final Map<UUID, Map<String, PermissionCache>> PERMISSION_CACHE = new ConcurrentHashMap<>();
	private static final ItemStack AIR = new ItemStack(Material.AIR);
	
	static {
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			long now = System.currentTimeMillis();
			Iterator<Entry<UUID, Map<String, PermissionCache>>> itr0 = PERMISSION_CACHE.entrySet().iterator();
			while (itr0.hasNext()) {
				Entry<UUID, Map<String, PermissionCache>> entry = itr0.next();
				Map<String, PermissionCache> map = entry.getValue();
				if (map == null || map.isEmpty()) {
					itr0.remove();
				} else {
					Iterator<PermissionCache> itr1 = map.values().iterator();
					while (itr1.hasNext()) {
						PermissionCache permissionCache = itr1.next();
						if (permissionCache.getTime() + 180000 < now) {
							itr1.remove();
						}
					}
				}
			}
		}, 0, 600);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		PERMISSION_CACHE.remove(event.getPlayer().getUniqueId());
	}
	
	public static boolean hasPermission(UUID uuid, String permission, boolean def, int timeout) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			Map<String, PermissionCache> map = PERMISSION_CACHE.get(uuid);
			if (map == null) {
				PERMISSION_CACHE.putIfAbsent(uuid, new ConcurrentHashMap<>());
				map = PERMISSION_CACHE.get(uuid);
			}
			PermissionCache cachedResult = map.get(permission);
			boolean result = cachedResult != null ? cachedResult.getValue() : player.hasPermission(permission);
			if (cachedResult == null) {
				map.put(permission, new PermissionCache(result, System.currentTimeMillis()));
			} else {
				cachedResult.setValue(result);
			}
			return result;
		} else {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
				Map<String, PermissionCache> map = PERMISSION_CACHE.get(uuid);
				if (map == null) {
					PERMISSION_CACHE.putIfAbsent(uuid, new ConcurrentHashMap<>());
					map = PERMISSION_CACHE.get(uuid);
				}
				PermissionCache cachedResult = map.get(permission);
				boolean result = cachedResult != null ? cachedResult.getValue() : InteractiveChat.perms.playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), permission);
				future.complete(result);
				if (cachedResult == null) {
					map.put(permission, new PermissionCache(result, System.currentTimeMillis()));
				} else {
					cachedResult.setValue(result);
				}
			});
			try {
				return future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				return def; 
			}
		}
	}
	
	public static void resetAllPermissionCache() {
		PERMISSION_CACHE.clear();
	}
	
	public static void resetPermissionCache(UUID uuid) {
		PERMISSION_CACHE.remove(uuid);
	}
	
	public static ItemStack getHeldItem(Player player) {
		return getHeldItem(ICPlayerFactory.getICPlayer(player));
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
