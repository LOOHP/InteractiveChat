package com.loohp.interactivechat.Utils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;

public class PlayerUtils {
	
	public static boolean hasPermission(UUID uuid, String permission, boolean def, int timeout) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			return player.hasPermission(permission);
		} else {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> future.complete(InteractiveChat.perms.playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), permission)));
			try {
				return future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				return def; 
			}
		}
	}

}
