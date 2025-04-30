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

package com.loohp.interactivechat.objectholders;

import org.bukkit.entity.Player;

public class OutboundPacket {

    private final Player reciever;
    private final Object packet;

    /**
     * Simple outbound packet class, used to store packets that will be sent later on to a player.
     *
     * @param reciever Receiver of the packet.
     * @param packet Will be cast to the platform-dependent object later. For ProtocolLib: PacketContainer. For PacketEvents: PacketWrapper.
     */
    public OutboundPacket(Player reciever, Object packet) {
        this.reciever = reciever;
        this.packet = packet;
    }

    public Player getReciever() {
        return reciever;
    }

    public Object getPacket() {
        return packet;
    }

}
