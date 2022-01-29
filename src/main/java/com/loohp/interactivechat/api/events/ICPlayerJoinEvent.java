package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;
import org.bukkit.event.HandlerList;

/**
 * Called when a icplayer joins
 *
 * @author LOOHP
 */
public class ICPlayerJoinEvent extends ICPlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ICPlayerJoinEvent(ICPlayer player, boolean isRemote) {
        super(player, isRemote);
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
