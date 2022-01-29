package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This is the base class of all events related to offlineicplayers.
 *
 * @author LOOHP
 */
public class OfflineICPlayerEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    protected final OfflineICPlayer player;

    public OfflineICPlayerEvent(OfflineICPlayer player) {
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
