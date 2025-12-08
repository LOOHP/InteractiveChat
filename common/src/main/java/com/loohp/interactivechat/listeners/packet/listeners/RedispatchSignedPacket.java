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

package com.loohp.interactivechat.listeners.packet.listeners;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.RedispatchedSignPacketHandler;
import com.loohp.interactivechat.platform.PlatformPacketEvent;
import com.loohp.interactivechat.platform.PlatformPacketListenerPriority;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatCommandPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerServerDataPacket;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import org.bukkit.entity.Player;

public class RedispatchSignedPacket {

    public static void packetListener() {
        InteractiveChat.protocolPlatform.getPlatformPacketListenerProvider().listenToPlayClientChat(InteractiveChat.plugin, PlatformPacketListenerPriority.MONITOR, event -> {
            if (shouldIgnoreEvent(event)) {
                return;
            }
            Player player = event.getPlayer();
            PlatformPlayClientChatPacket<?> packet = event.getPacket();
            handleChatPacket(event, player, packet);
        });
        InteractiveChat.protocolPlatform.getPlatformPacketListenerProvider().listenToPlayChatCommand(InteractiveChat.plugin, PlatformPacketListenerPriority.MONITOR, event -> {
            if (shouldIgnoreEvent(event)) {
                return;
            }
            Player player = event.getPlayer();
            PlatformPlayClientChatCommandPacket<?> packet = event.getPacket();
            handleChatCommandPacket(event, player, packet);
        });
        InteractiveChat.protocolPlatform.getPlatformPacketListenerProvider().listenToPlayServerServerData(InteractiveChat.plugin, PlatformPacketListenerPriority.MONITOR, event -> {
            if (shouldIgnoreEvent(event)) {
                return;
            }
            handleServerDataPacket(event);
        });
    }

    private static boolean shouldIgnoreEvent(PlatformPacketEvent<?, ?, ?> event) {
        return event.isPlayerTemporary() || event.isCancelled() || !InteractiveChat.protocolPlatform.hasChatSigning();
    }

    private static void handleChatPacket(PlatformPacketEvent<?, ?, ? extends PlatformPlayClientChatPacket<?>> event, Player player, PlatformPlayClientChatPacket<?> packet) {
        if (InteractiveChat.forceUnsignedChatPackets) {
            String message = packet.getMessage();
            if (message.startsWith("/")) {
                redispatchCommand(event, player, message);
            } else {
                redispatchChatMessage(event, player, message);
            }
        }
    }

    private static void handleChatCommandPacket(PlatformPacketEvent<?, ?, ? extends PlatformPlayClientChatCommandPacket<?>> event, Player player, PlatformPlayClientChatCommandPacket<?> packet) {
        if (InteractiveChat.forceUnsignedChatCommandPackets && packet.hasArgumentSignatureEntries()) {
            String command = "/" + packet.getCommand();
            redispatchCommand(event, player, command);
        }
    }

    private static void redispatchCommand(PlatformPacketEvent<?, ?, ?> event, Player player, String command) {
        event.setReadOnly(false);
        event.setCancelled(true);
        event.setReadOnly(true);

        RedispatchedSignPacketHandler.redispatchCommand(player, command);
    }

    private static void redispatchChatMessage(PlatformPacketEvent<?, ?, ?> event, Player player, String message) {
        if (!ModernChatSigningUtils.isChatMessageIllegal(message)) {
            event.setReadOnly(false);
            event.setCancelled(true);
            event.setReadOnly(true);

            RedispatchedSignPacketHandler.redispatchChatMessage(player, message);
        }
    }

    private static void handleServerDataPacket(PlatformPacketEvent<?, ?, ? extends PlatformPlayServerServerDataPacket<?>> event) {
        PlatformPlayServerServerDataPacket<?> packet = event.getPacket();
        if (InteractiveChat.hideServerUnsignedStatus) {
            packet.setServerUnsignedStatus(true);
        }
    }
}
