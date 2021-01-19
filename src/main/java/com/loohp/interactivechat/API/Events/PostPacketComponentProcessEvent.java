package com.loohp.interactivechat.API.Events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.comphenix.protocol.events.PacketContainer;

public class PostPacketComponentProcessEvent extends Event implements Cancellable {
	
	//This Event is called after the plugin as modified the modifiers in the chat packet and is ready to send

	private Player reciever;
	private PacketContainer packet;
	private UUID sender;
	private PacketContainer original;
	private PacketContainer originalModified;
	private boolean sendOriginalIfCancelled;
    private boolean cancel;

    public PostPacketComponentProcessEvent(boolean async, Player reciever, PacketContainer packet, UUID sender, PacketContainer original, boolean sendOriginalIfCancelled, boolean cancelled) {
    	super(async);
        this.reciever = reciever;
        this.packet = packet;
        this.sender = sender;
        this.original = original;
        this.sendOriginalIfCancelled = sendOriginalIfCancelled;
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

    public PacketContainer getOriginal() {
    	if (originalModified == null) {
    		originalModified = original.deepClone();
    	}
		return originalModified;
	}

	public boolean sendOriginalIfCancelled() {
		return sendOriginalIfCancelled;
	}
	
	public void setSendOriginalIfCancelled(boolean value) {
		this.sendOriginalIfCancelled = value;
	}

	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}

