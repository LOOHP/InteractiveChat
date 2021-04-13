package com.loohp.interactivechat.hooks.venturechat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;

import mineverse.Aust1n46.chat.listeners.PacketListener;

public class VentureChatInjection implements Listener {
	
	private static boolean init = false;
	private static PacketListener packetListener = null;
	
	public static void _init_() {
		Bukkit.getPluginManager().registerEvents(new VentureChatInjection(), InteractiveChat.plugin);
		InteractiveChat.protocolManager.getPacketListeners().forEach(each -> {
    		if (each.getPlugin().getName().equals("VentureChat")) {
    			ListeningWhitelist whitelist = each.getSendingWhitelist();
    			if (whitelist.getTypes().contains(PacketType.Play.Server.CHAT)) {
    				if (whitelist.getPriority().equals(ListenerPriority.MONITOR)) {
    					InteractiveChat.protocolManager.removePacketListener(each);
    					return;
    				}
    			}
    		}
    	});
		init = true;
	}
	
	public static void firePacketListener(PacketEvent event) {
		if (packetListener == null) {
			packetListener = new PacketListener();
		}
		packetListener.onPacketSending(event);
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (init) {
			if (event.getPlugin().getName().equalsIgnoreCase("VentureChat")) {
				Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
					_init_();
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into VentureChat!");
				}, 100);
			}
		}
	}

}
