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

import com.loohp.interactivechat.platform.packets.PlatformPacket;
import org.bukkit.entity.Player;

public class OutboundPacket<Packet> {

    private final Player receiver;
    private final PlatformPacket<Packet> packet;

    public OutboundPacket(Player receiver, PlatformPacket<Packet> packet) {
        this.receiver = receiver;
        this.packet = packet;
    }

    public Player getReceiver() {
        return receiver;
    }

    public PlatformPacket<Packet> getPacket() {
        return packet;
    }

}
