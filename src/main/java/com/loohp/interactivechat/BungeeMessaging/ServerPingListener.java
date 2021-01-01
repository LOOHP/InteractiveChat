package com.loohp.interactivechat.BungeeMessaging;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.json.simple.JSONObject;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.netty.SocketAdapter;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.loohp.interactivechat.InteractiveChat;

@SuppressWarnings("unchecked")
public class ServerPingListener implements Listener {
	
	public static final String INTERACTIVECHAT_PROTOCOL_IDENTIFIER = "InterativeChatBungeePing";
	public static final Map<InetAddress, Long> REQUESTS = new ConcurrentHashMap<>();
	public static String json;
	
	static {
		JSONObject json = new JSONObject();
		json.put("present", true);
		json.put("version", InteractiveChat.plugin.getDescription().getVersion());
		ServerPingListener.json = json.toJSONString();
	}
	
	public static void listen() {
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(InteractiveChat.plugin).types(PacketType.Handshake.Client.SET_PROTOCOL)) {
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				String str = packet.getStrings().read(0);
				if (str != null && str.equals(INTERACTIVECHAT_PROTOCOL_IDENTIFIER) && event.isPlayerTemporary()) {
					SocketAdapter socket;
					try {
						socket = (SocketAdapter) TemporaryPlayerFactory.getInjectorFromPlayer(event.getPlayer()).getSocket();
						REQUESTS.put(socket.getInetAddress(), System.currentTimeMillis() + 5000);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			Iterator<Entry<InetAddress, Long>> itr = REQUESTS.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<InetAddress, Long> entry = itr.next();
				if (System.currentTimeMillis() > entry.getValue()) {
					itr.remove();
				}
			}
		}, 0, 20);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerListPing(ServerListPingEvent event) {
		if (REQUESTS.remove(event.getAddress()) != null) {
			event.setMotd(json);
		}
	}

}
