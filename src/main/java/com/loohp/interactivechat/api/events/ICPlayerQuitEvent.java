package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;
import org.bukkit.event.HandlerList;

/**
 * Called when a icplayer is quits
 *
 * @author LOOHP
 */
public class ICPlayerQuitEvent extends ICPlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ICPlayerQuitEvent(ICPlayer player, boolean isRemote) {
        super(player, isRemote);
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
