/*
 * This file is part of InteractiveChat4.
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * This event is called whenever the item placeholder is used. Only the itemStack
 * can be changed in this event, nothing else. Changing the Component or
 * Canceling the event will cause UnsupportedOperationException to be thrown.
 *
 * @author LOOHP
 */
public class ItemPlaceholderEvent extends PlaceholderEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

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

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
