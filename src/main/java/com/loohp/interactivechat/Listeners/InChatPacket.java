package com.loohp.interactivechat.Listeners;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.CompatibilityListener;

public class InChatPacket {
	
	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	
	public static void chatMessageListener() {
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.LOWEST).types(PacketType.Play.Client.CHAT)) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (!event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Client.CHAT) || event.isPlayerTemporary()) {
		    		return;
		    	}
				
				PacketContainer packet = event.getPacket();
				Player player = event.getPlayer();
				String message = packet.getStrings().read(0);
				
				if (message != null && (message.matches(".*<cmd=" + UUID_REGEX + ">.*") || message.matches(".*<chat=" + UUID_REGEX + ">.*"))) {
					message = message.replaceAll("<cmd=" + UUID_REGEX + ">", "").replaceAll("<chat=" + UUID_REGEX + ">", "").trim();
					AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(true, player, message, new HashSet<>());
					RegisteredListener[] listeners = chatEvent.getHandlers().getRegisteredListeners();
					
					for (RegisteredListener registration : listeners) {
			            if (!registration.getPlugin().isEnabled()) {
			                continue;
			            }
			            String pluginName = registration.getPlugin().getName();
			            CompatibilityListener compatibilityListener = null;
			            for (Entry<String, CompatibilityListener> entry : InteractiveChat.compatibilityListeners.entrySet()) {
			            	if (pluginName.matches(entry.getKey())) {
			            		compatibilityListener = entry.getValue();
			            		break;
			            	}
			            }
			            if (compatibilityListener == null) {
			            	continue;
			            }
			            if (!registration.getPriority().equals(compatibilityListener.getPriority())) {
			            	continue;
			            }
			            if (!registration.getListener().getClass().getName().matches(compatibilityListener.getClassName())) {
			            	continue;
			            }
			            
			            try {
			                registration.callEvent(chatEvent);
			            } catch (AuthorNagException ex) {
			                Plugin plugin = registration.getPlugin();

			                if (plugin.isNaggable()) {
			                    plugin.setNaggable(false);

			                    Bukkit.getLogger().log(Level.SEVERE, String.format(
			                            "Nag author(s): '%s' of '%s' about the following: %s",
			                            plugin.getDescription().getAuthors(),
			                            plugin.getDescription().getFullName(),
			                            ex.getMessage()
			                            ));
			                }
			            } catch (Throwable ex) {
			            	Bukkit.getLogger().log(Level.SEVERE, "Could not pass event " + chatEvent.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), ex);
			            }
			        }
				}
			}
		});
	}

}
