package com.loohp.interactivechat.hooks.dynmap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.Client;
import org.dynmap.DynmapCore;
import org.dynmap.common.DynmapListenerManager.ChatEventListener;
import org.dynmap.common.DynmapPlayer;

import com.loohp.interactivechat.modules.ProcessExternalMessage;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class DynmapCoreChatListener implements ChatEventListener {
	
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
        		msg = msg.replaceAll(Registry.ID_PATTERN.pattern(), "");
        		core.mapManager.pushUpdate(new Client.ChatMessage("player", "", p.getDisplayName(), msg, p.getName()));
        	} else {
				try {
					String component = InteractiveChatComponentSerializer.gson().serialize(LegacyComponentSerializer.legacySection().deserialize(msg));
		        	String processed = PlainTextComponentSerializer.plainText().serialize(InteractiveChatComponentSerializer.gson().deserialize(ProcessExternalMessage.processAndRespond(bukkitplayer, component)));
		        	core.mapManager.pushUpdate(new Client.ChatMessage("player", "", p.getDisplayName(), processed, p.getName()));
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }
	}
	
}
