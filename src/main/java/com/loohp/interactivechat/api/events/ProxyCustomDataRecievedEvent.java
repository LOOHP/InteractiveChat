package com.loohp.interactivechat.api.events;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This is fired when a custom data packet is recieved from the proxy
 * @author LOOHP
 *
 */
public class ProxyCustomDataRecievedEvent extends Event {
	
	private final String channel;
	private final byte[] data;
	
	public ProxyCustomDataRecievedEvent(String channel, byte[] data) {
		super(!Bukkit.isPrimaryThread());
		this.channel = channel;
		this.data = data;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public byte[] getData() {
		return Arrays.copyOf(data, data.length);
	}

	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
