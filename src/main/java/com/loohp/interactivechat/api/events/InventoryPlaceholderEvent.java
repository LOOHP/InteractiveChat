package com.loohp.interactivechat.api.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.objectholders.ICPlayer;

import net.kyori.adventure.text.Component;

/**
 * This event is called whenever the inventory (and enderchest) placeholder is
 * used. Only the inventory can be changed in this event, nothing else. Inventory
 * cannot be null. Changing the Component or Canceling the event will cause
 * UnsupportedOperationException to be thrown.
 * @author LOOHP
 *
 */
public class InventoryPlaceholderEvent extends PlaceholderEvent {

	private Inventory inventory;
	private final InventoryPlaceholderType type;

	public InventoryPlaceholderEvent(ICPlayer sender, Player receiver, Component component, long timeSent, Inventory inventory, InventoryPlaceholderType type) {
		super(sender, receiver, component, timeSent);
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
	public Component getComponent() {
		return component;
	}

	@Override
	@Deprecated
	public void setComponent(Component component) {
		throw new UnsupportedOperationException("Changing the Component in this event is not allowed");
	}

	@Override
	@Deprecated
	public void setCancelled(boolean cancel) {
		throw new UnsupportedOperationException("Cancelling this event is not allowed");
	}

	public static enum InventoryPlaceholderType {
		INVENTORY, INVENTORY1_UPPER, INVENTORY1_LOWER, ENDERCHEST;
	}

}
