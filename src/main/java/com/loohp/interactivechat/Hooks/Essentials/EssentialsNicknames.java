package com.loohp.interactivechat.Hooks.Essentials;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.earth2me.essentials.Essentials;
import com.loohp.interactivechat.InteractiveChat;

import net.ess3.api.events.NickChangeEvent;

public class EssentialsNicknames implements Listener {
	
	private static Essentials essen;
	private static String prefix;
	
	public static void setup() {
		essen = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		prefix = essen.getConfig().getString("nickname-prefix");
	}
	
	public static void loadNicks(Player player) {
		if (essen.getUser(player.getUniqueId()).getNickname() != null && !essen.getUser(player.getUniqueId()).getNickname().equals("")) {
			String essentialsNick = essen.getUser(player.getUniqueId()).getNickname();
			InteractiveChat.essenNick.put(player, prefix + essentialsNick);
		}
	}
	
	@EventHandler
	public void onEssentialsReload(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().equalsIgnoreCase("/essentials reload")) {
			if (event.getPlayer().hasPermission("essentials.essentials")) {
				Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
					prefix = essen.getConfig().getString("nickname-prefix");
				}, 40);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEssentialsNickChange(NickChangeEvent event) {
		try {
			InteractiveChat.essenNick.put(event.getAffected().getBase(), prefix + event.getValue());
		} catch (Exception ignore) {}
	}
	
	@EventHandler
	public void onEssentialsJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
			loadNicks(player);
		}, 100);
	}
	
	@EventHandler
	public void onEssentialsLeave(PlayerQuitEvent event) {
		InteractiveChat.essenNick.remove(event.getPlayer());
	}

}
