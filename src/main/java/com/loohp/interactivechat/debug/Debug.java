package com.loohp.interactivechat.debug;

import com.loohp.interactivechat.InteractiveChat;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Debug implements Listener {

    @EventHandler
    public void onJoinPluginActive(PlayerJoinEvent event) {
        if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEshakE")) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "InteractiveChat " + InteractiveChat.plugin.getDescription().getVersion() + " is running!");
        }
    }

}
