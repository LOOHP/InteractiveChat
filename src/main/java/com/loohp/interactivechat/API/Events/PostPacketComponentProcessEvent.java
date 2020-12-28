package com.loohp.interactivechat.API.Events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.comphenix.protocol.events.PacketContainer;

public class PostPacketComponentProcessEvent extends Event implements Cancellable {
	
	//This Event is called after the plugin as modified the modifiers in the chat packet and is ready to send

	Player reciever;
	PacketContainer packet;
	UUID sender;
    boolean cancel;

    public PostPacketComponentProcessEvent(boolean async, Player reciever, PacketContainer packet, UUID sender, boolean cancelled) {
    	super(async);
        this.reciever = reciever;
        this.packet = packet;
        this.sender = sender;
        this.cancel = cancelled;
    }
    
    @Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
    
    public Player getReciver() {
    	return reciever;
    }
    
    public UUID getSender() {
    	return sender;
    }
    
    public PacketContainer getPacket() {
    	return packet;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

