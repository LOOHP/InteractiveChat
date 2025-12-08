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

package com.loohp.interactivechat.bungeemessaging;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.platform.PlatformPacketListenerPriority;
import com.loohp.interactivechat.platform.packets.PlatformHandshakeClientSetProtocolPacket;
import com.loohp.interactivechat.platform.packets.PlatformStatusServerServerInfoPacket;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.platformscheduler.Scheduler;

import static com.loohp.interactivechat.bungeemessaging.ServerPingListenerUtils.MOTD_JSON;
import static com.loohp.interactivechat.bungeemessaging.ServerPingListenerUtils.REQUESTS;

public class ServerPingListener {

    public static void listen() {
        InteractiveChat.protocolPlatform.getPlatformPacketListenerProvider().listenToHandshakeClientSetProtocol(InteractiveChat.plugin, PlatformPacketListenerPriority.NORMAL, event -> {
            PlatformHandshakeClientSetProtocolPacket<?> packet = event.getPacket();
            String str = packet.getServerAddress();
            if (str != null && str.equals(Registry.PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER) && event.isPlayerTemporary()) {
                REQUESTS.put(event.getIdentityObject(), System.currentTimeMillis() + 5000);
            }
        });
        InteractiveChat.protocolPlatform.getPlatformPacketListenerProvider().listenToStatusServerServerInfo(InteractiveChat.plugin, PlatformPacketListenerPriority.NORMAL, event -> {
            PlatformStatusServerServerInfoPacket<?> packet = event.getPacket();
            String motd = packet.getMotd();
            if (event.isPlayerTemporary() && REQUESTS.remove(event.getIdentityObject()) != null && motd != null) {
                packet.setMotd(MOTD_JSON);
            }
        });
        Scheduler.runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
            REQUESTS.entrySet().removeIf(entry -> System.currentTimeMillis() > entry.getValue());
        }, 0, 20);
    }

}
