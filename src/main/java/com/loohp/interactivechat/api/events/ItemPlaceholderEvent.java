package com.loohp.interactivechat.api.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.objectholders.ICPlayer;

import net.kyori.adventure.text.Component;

/**
 * This event is called whenever the item placeholder is used. Only the itemStack
 * can be changed in this event, nothing else. Changing the Component or
 * Canceling the event will cause UnsupportedOperationException to be thrown.
 * @author LOOHP
 *
 */
public class ItemPlaceholderEvent extends PlaceholderEvent {

	private ItemStack itemStack;

	public ItemPlaceholderEvent(ICPlayer sender, Player receiver, Component component, long timeSent, ItemStack itemStack) {
		super(sender, receiver, component, timeSent);
		this.itemStack = itemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack itemStack) {
		if (itemStack == null) {
			itemStack = new ItemStack(Material.AIR);
		}
		this.itemStack = itemStack;
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

}
