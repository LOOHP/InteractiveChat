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

package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.platform.packets.PlatformPacket;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface ProtocolPlatform<PacketEvent, Packet> {

    Plugin getRegisteredPlugin();

    Plugin getProtocolPlatformPlugin();

    boolean hasChatSigning();

    int getProtocolVersion(Player player);

    Player newTemporaryPlayer(String name, UUID uuid);

    default void sendServerPacket(Player player, PlatformPacket<?> packet) {
        sendServerPacket(player, packet, true);
    }

    void sendServerPacket(Player player, PlatformPacket<?> packet, boolean filtered);

    PlatformPacketListenerProvider<PacketEvent, Packet> getPlatformPacketListenerProvider();

    PlatformPacketCreatorProvider<Packet> getPlatformPacketCreatorProvider();

}
