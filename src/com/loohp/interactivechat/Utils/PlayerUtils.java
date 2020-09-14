package com.loohp.interactivechat.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.loohp.interactivechat.InteractiveChat;

public class PlayerUtils implements Listener {
	
	private static Map<UUID, Map<String, Boolean>> cache = new HashMap<>();
	
	static {
		Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
			cache.clear();
		}, 0, 600);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		cache.remove(event.getPlayer().getUniqueId());
	}
	
	public static boolean hasPermission(UUID uuid, String permission, boolean def, int timeout) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			return player.hasPermission(permission);
		} else {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
				Map<String, Boolean> map = cache.get(uuid);
				if (map == null) {
					map = new HashMap<>();
					cache.put(uuid, map);
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

}
