package com.loohp.interactivechat.listeners;

import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PaperLoginEvents implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinConfirm(PlayerJoinEvent event) {
        ICPlayerFactory.getUnsafe().triggerPlayerJoinEarly(event.getPlayer());
        PlayernameDisplay.resetCache();
        ICPlayerFactory.getUnsafe().triggerPlayerJoinConfirm(event.getPlayer(), true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuitFinal(PlayerQuitEvent event) {
        PlayernameDisplay.resetCache();
        ICPlayerFactory.getUnsafe().triggerPlayerQuit(event.getPlayer());
    }

}
