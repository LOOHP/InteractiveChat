package com.loohp.interactivechat.Hooks.VentureChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.loohp.interactivechat.InteractiveChat;

public class VentureChatInjection implements Listener {
	
	private static boolean init = false;
	
	public static void _init_() {
		Bukkit.getPluginManager().registerEvents(new VentureChatInjection(), InteractiveChat.plugin);
		Plugin ventureChat = Bukkit.getPluginManager().getPlugin("VentureChat");
		InteractiveChat.protocolManager.getPacketListeners().forEach(each -> {
    		if (each.getPlugin().getName().equals("VentureChat")) {
    			ListeningWhitelist whitelist = each.getSendingWhitelist();
    			if (whitelist.getTypes().contains(PacketType.Play.Server.CHAT)) {
    				if (whitelist.getPriority().equals(ListenerPriority.MONITOR)) {
    					InteractiveChat.protocolManager.removePacketListener(each);
    					InteractiveChat.protocolManager.addPacketListener(new PacketListener(ventureChat));
    					return;
    				}
    			}
    		}
    	});
		init = true;
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
