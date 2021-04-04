package com.loohp.interactivechat.Hooks.Dynmap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.dynmap.DynmapCore;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.common.DynmapListenerManager;
import org.dynmap.common.DynmapListenerManager.EventListener;
import org.dynmap.common.DynmapListenerManager.EventType;

import com.loohp.interactivechat.InteractiveChat;

public class DynmapListener implements Listener {
	
	private static boolean init = false;
	
	@SuppressWarnings("unchecked")
	public static void _init_() {
		Bukkit.getPluginManager().registerEvents(new DynmapListener(), InteractiveChat.plugin);
		try {
			DynmapPlugin dynmapPlugin = DynmapPlugin.plugin;
			Field coreField = dynmapPlugin.getClass().getDeclaredField("core");
			coreField.setAccessible(true);
			DynmapCore dynmapCore = (DynmapCore) coreField.get(dynmapPlugin);
			coreField.setAccessible(false);
			DynmapListenerManager dynmapEvents = dynmapCore.listenerManager;
			Field listenerField = dynmapEvents.getClass().getDeclaredField("listeners");
			listenerField.setAccessible(true);
			Map<EventType, ArrayList<EventListener>> listeners = (Map<EventType, ArrayList<EventListener>>) listenerField.get(dynmapEvents);
			listenerField.setAccessible(false);
			listeners.remove(EventType.PLAYER_CHAT);
			dynmapEvents.addListener(EventType.PLAYER_CHAT, new DynmapCoreChatListener(dynmapCore));
			init = true;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (init) {
			if (event.getPlugin().getName().equalsIgnoreCase("dynmap")) {
				Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
					_init_();
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into Dynmap!");
				}, 100);
			}
		}
	}

}
