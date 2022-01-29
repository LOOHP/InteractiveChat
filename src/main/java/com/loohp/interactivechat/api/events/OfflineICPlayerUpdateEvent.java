package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import org.bukkit.event.HandlerList;

/**
 * This is fired when an offlineICPlayer is updated
 *
 * @author LOOHP
 */
public class OfflineICPlayerUpdateEvent extends OfflineICPlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OfflineICPlayerUpdateEvent(OfflineICPlayer player) {
        super(player);
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
