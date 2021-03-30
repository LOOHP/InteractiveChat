package com.loohp.interactivechat.Hooks.Dynmap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.Client;
import org.dynmap.DynmapCore;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.common.DynmapListenerManager;
import org.dynmap.common.DynmapListenerManager.ChatEventListener;
import org.dynmap.common.DynmapListenerManager.EventListener;
import org.dynmap.common.DynmapListenerManager.EventType;
import org.dynmap.common.DynmapPlayer;

import com.loohp.interactivechat.Modules.ProcessExternalMessage;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class DynmapListener implements ChatEventListener {
	
	@SuppressWarnings("unchecked")
	public static void _init_() {
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
			dynmapEvents.addListener(EventType.PLAYER_CHAT, new DynmapListener(dynmapCore));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	
	private DynmapCore core;
	
	public DynmapListener(DynmapCore core) {
		this.core = core;
	}

	@Override
	public void chatEvent(DynmapPlayer p, String msg) {
		if (core.disable_chat_to_web) {
			return;
		}
        if (core.mapManager != null) {
        	Player bukkitplayer = Bukkit.getPlayer(p.getUUID());
        	if (bukkitplayer == null) {
        		msg = msg.replaceAll("<cmd=" + UUID_REGEX + ">", "").replaceAll("<chat=" + UUID_REGEX + ">", "");
        		core.mapManager.pushUpdate(new Client.ChatMessage("player", "", p.getDisplayName(), msg, p.getName()));
        	} else {
				try {
					String component = ComponentSerializer.toString(new TextComponent(msg));
		        	String processed = ComponentSerializer.parse(ProcessExternalMessage.processAndRespond(bukkitplayer, component))[0].toPlainText();
		        	core.mapManager.pushUpdate(new Client.ChatMessage("player", "", p.getDisplayName(), processed, p.getName()));
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }
	}

}
