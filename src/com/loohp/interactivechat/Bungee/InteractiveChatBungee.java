package com.loohp.interactivechat.Bungee;

import java.net.SocketAddress;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class InteractiveChatBungee extends Plugin implements Listener {
	
	public static Plugin plugin = null;
			
	@Override
    public void onEnable() {
		plugin = ProxyServer.getInstance().getPluginManager().getPlugin("InteractiveChatBungee");
		
		getProxy().registerChannel("interactivechat:channel");		   
        getProxy().getPluginManager().registerListener(this, this);
	    
		getLogger().info(ChatColor.GREEN + "InteractiveChatBungee has been enabled!");
    } 
	
	@Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "InteractiveChatBungee has been disabled!");
    } 
	
    @EventHandler
    public void onReceive(PluginMessageEvent event) {

        if (!event.getTag().equals("interactivechat:channel")) {
            return;
        }

        SocketAddress senderServer = event.getSender().getSocketAddress();

        for (ServerInfo server : getProxy().getServers().values()) {
            if (!server.getSocketAddress().equals(senderServer) && server.getPlayers().size() > 0) {
                server.sendData("interactivechat:channel", event.getData());
            }
        }
    }
}