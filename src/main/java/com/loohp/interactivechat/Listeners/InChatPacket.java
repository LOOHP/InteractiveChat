package com.loohp.interactivechat.Listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;

public class InChatPacket {
	
	public static void chatMessageListener() {
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.LOWEST).types(PacketType.Play.Client.CHAT)) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (!event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Client.CHAT) || event.isPlayerTemporary()) {
		    		return;
		    	}
				
				PacketContainer packet = event.getPacket();
				Player player = event.getPlayer();
				UUID uuid = player.getUniqueId();
				String message = packet.getStrings().read(0);
				System.out.println(Arrays.toString(message.getBytes()));
				System.out.println(Arrays.toString("|-".getBytes()));
				
				List<String> lines = InteractiveChat.lineInputs.get(uuid);
				
				System.out.println(lines == null);
				
				if (lines == null) {
					if (message.equals("\\|-")) {
						packet.getStrings().write(0, "|-");
					} else if (message.equals("|-")) {
						event.setCancelled(true);
						InteractiveChat.lineInputs.put(uuid, new ArrayList<>());
					}
				} else {
					if (!message.startsWith("/")) {
						event.setCancelled(true);
						if (message.equals("\\==")) {
							lines.add("//");
						} else if (message.equals("==")) {
							InteractiveChat.lineInputs.remove(uuid);
							PacketContainer newPacket = InteractiveChat.protocolManager.createPacket(PacketType.Play.Client.CHAT);
							newPacket.getStrings().write(0, String.join("\n", lines));
							try {
								InteractiveChat.protocolManager.recieveClientPacket(player, newPacket);
							} catch (IllegalAccessException | InvocationTargetException e) {
								e.printStackTrace();
							}
						} else {
							lines.add(message);
						}
					}
				}
			}
		});
	}

}
