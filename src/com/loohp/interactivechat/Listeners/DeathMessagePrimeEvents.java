package com.loohp.interactivechat.Listeners;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;

import email.com.gmail.cosmoconsole.bukkit.deathmsg.DeathMessageBroadcastEvent;
import email.com.gmail.cosmoconsole.bukkit.deathmsg.DeathMessagesPrime;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class DeathMessagePrimeEvents implements Listener {
	
	@SuppressWarnings("unchecked")
	@EventHandler
	public void onDMPMessage(DeathMessageBroadcastEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		event.setCancelled(true);
		String message = event.getMessage().toLegacyText();
		
		TextComponent text = new TextComponent(message);	
    	String string = ComponentSerializer.toString(text);
		
		DeathMessagesPrime instance = (DeathMessagesPrime) Bukkit.getPluginManager().getPlugin("DeathMessagesPrime");
		@SuppressWarnings("rawtypes")
		Class clazz = instance.getClass();
		Field field = clazz.getDeclaredField("showdeath");
		field.setAccessible(true);
		Map<UUID, Boolean> showdeath = (HashMap<UUID, Boolean>) field.get(instance);
		
		if (event.getWorld() == null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (showdeath.containsKey(player.getUniqueId())) {
					if (showdeath.get(player.getUniqueId()) == true) {
						continue;
					} else {
		            	PacketContainer send = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
		            	send.getChatComponents().write(0, WrappedChatComponent.fromJson(string));
		            	try {
							InteractiveChat.protocolManager.sendServerPacket(player, send, true);
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				} else {
	            	PacketContainer send = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
	            	send.getChatComponents().write(0, WrappedChatComponent.fromJson(string));
	            	try {
						InteractiveChat.protocolManager.sendServerPacket(player, send, true);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			for (Player player : event.getWorld().getPlayers()) {
				if (showdeath.containsKey(player.getUniqueId())) {
					if (showdeath.get(player.getUniqueId()) == true) {
						continue;
					} else {
		            	PacketContainer send = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
		            	send.getChatComponents().write(0, WrappedChatComponent.fromJson(string));
		            	try {
							InteractiveChat.protocolManager.sendServerPacket(player, send, true);
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				} else {
	            	PacketContainer send = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
	            	send.getChatComponents().write(0, WrappedChatComponent.fromJson(string));
	            	try {
						InteractiveChat.protocolManager.sendServerPacket(player, send, true);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
