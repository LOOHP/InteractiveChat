package com.loohp.interactivechat.API.Events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * This Event is called after the plugin had determinded the chat component in
 * the packet need modifying and before the plugin actually modifies the
 * components. Sender will be null if the message is not send by a player or
 * the plugin is unable to find the sender of the message.
 * @author LOOHP
 *
 */
public class PrePacketComponentProcessEvent extends Event {

	// This Event is called after the plugin had determinded the chat component in
	// the packet need modifying
	// And before the plugin actually modifies the components
	// Sender will be null if the message is not send by a player or the plugin is
	// unable to find the sender of the message
	// Field is the field number in which the component is obtained from the chat
	// packet, either 0 or 1

	private Player reciever;
	private BaseComponent component;
	private UUID sender;

	public PrePacketComponentProcessEvent(boolean async, Player reciever, BaseComponent component, UUID sender) {
		super(async);
		this.reciever = reciever;
		this.component = component;
		this.sender = sender;
	}

	public Player getReciver() {
		return reciever;
	}

	public UUID getSender() {
		return sender;
	}

	public void setSender(UUID sender) {
		this.sender = sender;
	}

	public BaseComponent getBaseComponent() {
		return component;
	}

	public void setBaseComponent(BaseComponent component) {
		this.component = component;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

}
