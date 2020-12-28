package com.loohp.interactivechat.API.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;

import net.md_5.bungee.api.chat.BaseComponent;

public class PlaceholderEvent extends Event implements Cancellable {
	
	/*
	 * This is the base class of all events related to parsing placeholders
	 */
	
	protected final PlayerWrapper sender;
	protected final Player receiver;
	protected BaseComponent baseComponent;
	protected final long timeSent;
	protected boolean isCancelled;
	
	public PlaceholderEvent(PlayerWrapper sender, Player receiver, BaseComponent baseComponent, long timeSent) {
		super(!Bukkit.isPrimaryThread());
		this.sender = sender;
		this.receiver = receiver;
		this.baseComponent = baseComponent;
		this.timeSent = timeSent;
		this.isCancelled = false;
	}
	
	public PlaceholderEvent(Player receiver, BaseComponent baseComponent, long timeSent) {
		this(null, receiver, baseComponent, timeSent);
	}
	
	public boolean hasSender() {
		return sender != null;
	}
	
	public PlayerWrapper getSender() {
		return sender;
	}

	public Player getReceiver() {
		return receiver;
	}

	public BaseComponent getBaseComponent() {
		return baseComponent;
	}

	public void setBaseComponent(BaseComponent baseComponent) {
		this.baseComponent = baseComponent;
	}

	public long getTimeSent() {
		return timeSent;
	}
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
