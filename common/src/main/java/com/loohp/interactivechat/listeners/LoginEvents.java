package com.loohp.interactivechat.listeners;

import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginEvents implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinEarly(PlayerLoginEvent event) {
        ICPlayerFactory.getUnsafe().triggerPlayerJoinEarly(event.getPlayer());
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinConfirm(PlayerLoginEvent event) {
        boolean allowed = event.getResult().equals(PlayerLoginEvent.Result.ALLOWED);
        if (allowed) {
            PlayernameDisplay.resetCache();
        }
        ICPlayerFactory.getUnsafe().triggerPlayerJoinConfirm(event.getPlayer(), allowed);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuitFinal(PlayerQuitEvent event) {
        PlayernameDisplay.resetCache();
        ICPlayerFactory.getUnsafe().triggerPlayerQuit(event.getPlayer());
    }

}
