/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

/**
 * This event is called whenever the inventory (and enderchest) placeholder is
 * used. Only the inventory can be changed in this event, nothing else. Inventory
 * cannot be null. Changing the Component or Canceling the event will cause
 * UnsupportedOperationException to be thrown.
 *
 * @author LOOHP
 */
public class InventoryPlaceholderEvent extends PlaceholderEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final InventoryPlaceholderType type;
    private Inventory inventory;

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

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public enum InventoryPlaceholderType {
        INVENTORY, INVENTORY1_UPPER, INVENTORY1_LOWER, ENDERCHEST
    }

}
