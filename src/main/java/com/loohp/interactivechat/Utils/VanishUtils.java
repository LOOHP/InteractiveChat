package com.loohp.interactivechat.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactivechat.InteractiveChat;

import de.myzelyam.api.vanish.VanishAPI;

public class VanishUtils {
	
	private static Set<UUID> offlineVanish = new HashSet<>();
	private static long cacheTimeout = 0;
	
	public static boolean isVanished(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		boolean isOnline = player != null;
		if (InteractiveChat.vanishHook) {
			if (isOnline) {
				if (VanishAPI.isInvisible(player)) {
					return true;
				}
			} else {
				if (getOfflineVanish().contains(uuid)) {
					return true;
				}
			}
		}
		if (InteractiveChat.cmiHook) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(uuid);
			if (user != null && user.isVanished()) {
				return true;
			}
		}
		if (InteractiveChat.essentialsHook) {
			Essentials ess3 = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			User user = ess3.getUser(uuid);
			if (user != null && user.isVanished()) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private synchronized static Set<UUID> getOfflineVanish() {
		long time = System.currentTimeMillis();
		if (cacheTimeout < time) {
			offlineVanish = VanishAPI.getAllInvisiblePlayers().stream().collect(Collectors.toSet());
			cacheTimeout = time + 3000;
		}
		return offlineVanish;
	}

}
