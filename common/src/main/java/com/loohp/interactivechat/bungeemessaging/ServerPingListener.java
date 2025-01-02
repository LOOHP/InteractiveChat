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

package com.loohp.interactivechat.bungeemessaging;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ServerPingListener implements Listener {

    public static final Map<Player, Long> REQUESTS = new ConcurrentHashMap<>();
    public static String json;

    static {
        JSONObject json = new JSONObject();
        json.put("present", true);
        json.put("version", InteractiveChat.plugin.getDescription().getVersion());
        json.put("minecraftVersion", InteractiveChat.version.getNumber());
        json.put("exactMinecraftVersion", InteractiveChat.exactMinecraftVersion);
        json.put("protocol", Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION);
        ServerPingListener.json = json.toJSONString();
    }

    public static void listen() {
        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(InteractiveChat.plugin).types(PacketType.Handshake.Client.SET_PROTOCOL)) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                String str = packet.getStrings().read(0);
                if (str != null && str.equals(Registry.PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER) && event.isPlayerTemporary()) {
                    REQUESTS.put(event.getPlayer(), System.currentTimeMillis() + 5000);
                }
            }
        });
        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(InteractiveChat.plugin).types(PacketType.Status.Server.SERVER_INFO)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                WrappedServerPing response = packet.getServerPings().read(0);
                if (event.isPlayerTemporary() && REQUESTS.remove(event.getPlayer()) != null && response != null) {
                    response.setMotD(json);
                    packet.getServerPings().write(0, response);
                }
            }
        });

        InteractiveChat.plugin.getScheduler().runTimerAsync((task) -> {
            REQUESTS.entrySet().removeIf(entry -> System.currentTimeMillis() > entry.getValue());
        }, 0, 20);
    }

}
