package com.loohp.interactivechat.hooks.dynmap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.Client;
import org.dynmap.DynmapCore;
import org.dynmap.common.DynmapListenerManager.ChatEventListener;
import org.dynmap.common.DynmapPlayer;

import com.loohp.interactivechat.modules.ProcessExternalMessage;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class DynmapCoreChatListener implements ChatEventListener {

private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	
	private DynmapCore core;
	
	public DynmapCoreChatListener(DynmapCore core) {
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
