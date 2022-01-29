package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This is the base class of all events related to icplayers.
 *
 * @author LOOHP
 */
public class ICPlayerEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    protected final ICPlayer player;
    protected final boolean isRemote;

    public ICPlayerEvent(ICPlayer player, boolean isRemote) {
        super(!Bukkit.isPrimaryThread());
        this.player = player;
        this.isRemote = isRemote;
    }

    public ICPlayer getPlayer() {
        return player;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
