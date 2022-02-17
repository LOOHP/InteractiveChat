/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * This Event is called after the plugin as modified the modifiers in the chat packet and is ready to send.
 *
 * @author LOOHP
 */
public class PreChatPacketSendEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Player reciever;
    private final PacketContainer packet;
    private final UUID sender;
    private final PacketContainer original;
    private PacketContainer originalModified;
    private boolean sendOriginalIfCancelled;
    private boolean cancel;

    public PreChatPacketSendEvent(boolean async, Player reciever, PacketContainer packet, UUID sender, PacketContainer original, boolean sendOriginalIfCancelled, boolean cancelled) {
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

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}

