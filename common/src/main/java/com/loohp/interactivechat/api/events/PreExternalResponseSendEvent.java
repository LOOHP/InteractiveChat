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

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * This Event is called after the plugin has modified the components an external request and is ready to send.
 *
 * @author LOOHP
 */
public class PreExternalResponseSendEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Player reciever;
    private Component component;
    private final UUID sender;
    private Component original;
    private boolean sendOriginalIfCancelled;

    public PreExternalResponseSendEvent(boolean async, Player reciever, Component component, UUID sender, Component original, boolean sendOriginalIfCancelled) {
        super(async);
        this.reciever = reciever;
        this.component = component;
        this.sender = sender;
        this.original = original;
        this.sendOriginalIfCancelled = sendOriginalIfCancelled;
    }

    public Player getReciever() {
        return reciever;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public UUID getSender() {
        return sender;
    }

    public Component getOriginal() {
        return original;
    }

    public void setOriginal(Component original) {
        this.original = original;
    }

    public boolean isSendOriginalIfCancelled() {
        return sendOriginalIfCancelled;
    }

    public void setSendOriginalIfCancelled(boolean sendOriginalIfCancelled) {
        this.sendOriginalIfCancelled = sendOriginalIfCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}

