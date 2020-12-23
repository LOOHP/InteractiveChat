package com.loohp.interactivechat.API.Events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class InventoryPlaceholderEvent extends PlaceholderEvent {
	
	/*
	 * This event is called whenever the inventory (and enderchest) placeholder is used
	 * Only the inventory can be changed in this event, nothing else.
	 * Inventory cannot be null.
	 * Changing the BaseComponent or Canceling the event will cause UnsupportedOperationException to be thrown
	 */

	private Inventory inventory;
	private final InventoryPlaceholderType type;
	
	public InventoryPlaceholderEvent(PlayerWrapper sender, Player receiver, BaseComponent baseComponent, long timeSent, Inventory inventory, InventoryPlaceholderType type) {
		super(sender, receiver, baseComponent, timeSent);
		this.inventory = inventory;
		this.type = type;
	}
	
	public InventoryPlaceholderType getType() {
		return type;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		if (inventory == null) {
			throw new IllegalArgumentException("Inventory cannot be null");
		}
		this.inventory = inventory;
	}
	
	@Override
	public BaseComponent getBaseComponent() {
		return ComponentSerializer.parse(ComponentSerializer.toString(baseComponent))[0];
	}
	
	@Override
	@Deprecated
	public void setBaseComponent(BaseComponent baseComponent) {
		throw new UnsupportedOperationException("Changing the BaseComponent in this event is not allowed");
	}
	
	@Override
	@Deprecated
	public void setCancelled(boolean cancel) {
		throw new UnsupportedOperationException("Cancelling this event is not allowed");
	}
	
	public static enum InventoryPlaceholderType {
		INVENTORY,
		ENDERCHEST;
	}

}
