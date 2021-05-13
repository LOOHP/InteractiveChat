package com.loohp.interactivechat.bungeemessaging;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.json.simple.JSONObject;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.registry.Registry;

@SuppressWarnings("unchecked")
public class ServerPingListener implements Listener {
	
	public static final Map<Player, Long> REQUESTS = new ConcurrentHashMap<>();
	public static String json;
	
	static {
		JSONObject json = new JSONObject();
		json.put("present", true);
		json.put("version", InteractiveChat.plugin.getDescription().getVersion());
		json.put("minecraftVersion", InteractiveChat.version.getNumber());
		json.put("exactMinecraftVersion", InteractiveChat.exactMinecraftVersion);
		json.put("protocol", Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION);
		ServerPingListener.json = json.toJSONString();
	}
	
	public static void listen() {
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(InteractiveChat.plugin).types(PacketType.Handshake.Client.SET_PROTOCOL)) {			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				String str = packet.getStrings().read(0);
				if (str != null && str.equals(Registry.PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER) && event.isPlayerTemporary()) {
					REQUESTS.put(event.getPlayer(), System.currentTimeMillis() + 5000);
				}
			}			
		});
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(InteractiveChat.plugin).types(PacketType.Status.Server.SERVER_INFO)) {			
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				WrappedServerPing response = packet.getServerPings().read(0);
				if (event.isPlayerTemporary() && REQUESTS.remove(event.getPlayer()) != null && response != null) {
					response.setMotD(json);
					packet.getServerPings().write(0, response);
				}
			}
		});
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			Iterator<Entry<Player, Long>> itr = REQUESTS.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Player, Long> entry = itr.next();
				if (System.currentTimeMillis() > entry.getValue()) {
					itr.remove();
				}
			}
		}, 0, 20);
	}

}
