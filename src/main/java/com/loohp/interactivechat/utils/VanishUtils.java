package com.loohp.interactivechat.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactivechat.InteractiveChat;

import de.myzelyam.api.vanish.VanishAPI;

public class VanishUtils {
	
	private static Object premiumVanishChatListener;
	private static Method premiumVanishChatListenerExecuteMethod;
	private static Set<UUID> offlineVanish = new HashSet<>();
	private static long cacheTimeout = 0;
	
	static {
		try {
			Plugin premiumVanish = Bukkit.getPluginManager().getPlugin("PremiumVanish");
			Class<?> premiumVanishChatListenerClass = Class.forName("de.myzelyam.premiumvanish.bukkit.listeners.ChatListener");
			premiumVanishChatListener = premiumVanishChatListenerClass.getConstructors()[0].newInstance(premiumVanish);
			premiumVanishChatListenerExecuteMethod = premiumVanishChatListenerClass.getMethod("execute", Listener.class, Event.class);
		} catch (Exception e) {
			premiumVanishChatListener = null;
			premiumVanishChatListenerExecuteMethod = null;
		}
	}
	
	public static Optional<String> checkChatIsCancelled(Player player, String message) {
		if (premiumVanishChatListener == null) {
			return Optional.of(message);
		} else {
			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(!Bukkit.isPrimaryThread(), player, message, new HashSet<>());
			try {
				premiumVanishChatListenerExecuteMethod.invoke(premiumVanishChatListener, null, event);
				return event.isCancelled() ? Optional.empty() : Optional.of(event.getMessage());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				return Optional.of(message);
			}
		}
	}
	
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
