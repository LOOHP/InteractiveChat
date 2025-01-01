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
 * This Event is called after the plugin had modified the
 * components of a chat packet. Sender will be null if the message is not send by a player or
 * the plugin is unable to find the sender of the message.
 *
 * @author LOOHP
 */
public class PostPacketComponentProcessEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Player receiver;
    private final UUID sender;
    private Component component;

    public PostPacketComponentProcessEvent(boolean async, Player receiver, Component component, UUID sender) {
        super(async);
        this.receiver = receiver;
        this.component = component;
        this.sender = sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public UUID getSender() {
        return sender;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
