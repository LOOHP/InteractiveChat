package com.loohp.interactivechat.api.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.loohp.interactivechat.objectholders.ICPlayer;

/**
 * This is the base class of all events related to icplayers.
 * @author LOOHP
 *
 */
public class RemovePlayerEvent extends Event {
	
	protected final ICPlayer player;
	
	public RemovePlayerEvent(ICPlayer player) {
		super(!Bukkit.isPrimaryThread());
		this.player = player;
	}
	
	public ICPlayer getPlayer() {
		return player;
	}

	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
