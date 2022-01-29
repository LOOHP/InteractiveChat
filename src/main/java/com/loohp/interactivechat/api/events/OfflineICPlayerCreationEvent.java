package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This is fired when an offlineICPlayer is created
 *
 * @author LOOHP
 */
public class OfflineICPlayerCreationEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    protected final OfflineICPlayer player;

    public OfflineICPlayerCreationEvent(OfflineICPlayer player) {
        super(!Bukkit.isPrimaryThread());
        this.player = player;
    }

    public OfflineICPlayer getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
