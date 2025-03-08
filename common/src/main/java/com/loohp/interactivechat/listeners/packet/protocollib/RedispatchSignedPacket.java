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

package com.loohp.interactivechat.listeners.packet.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.platform.protocollib.ProtocolLibPlatform;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class RedispatchSignedPacket {

    public static void packetListener() {
        PacketType[] packetTypes = getPacketTypes();
        ProtocolLibPlatform.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(packetTypes)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Do nothing
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (shouldIgnoreEvent(event)) {
                    return;
                }

                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();

                if (event.getPacketType().equals(PacketType.Play.Client.CHAT)) {
                    handleChatPacket(event, player, packet);
                } else {
                    handleChatCommandPacket(event, player, packet);
                }
            }
        });

        ProtocolLibPlatform.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.HIGH).types(PacketType.Play.Server.SERVER_DATA)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (shouldIgnoreEvent(event)) {
                    return;
                }

                handleServerDataPacket(event);
            }
        });
    }

    private static PacketType[] getPacketTypes() {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5)) {
            return new PacketType[]{PacketType.Play.Client.CHAT_COMMAND_SIGNED, PacketType.Play.Client.CHAT_COMMAND, PacketType.Play.Client.CHAT};
        } else {
            return new PacketType[]{PacketType.Play.Client.CHAT_COMMAND, PacketType.Play.Client.CHAT};
        }
    }

    private static boolean shouldIgnoreEvent(PacketEvent event) {
        return event.isPlayerTemporary() || event.isCancelled() || !InteractiveChat.protocolPlatform.hasChatSigning();
    }

    private static void handleChatPacket(PacketEvent event, Player player, PacketContainer packet) {
        if (InteractiveChat.forceUnsignedChatPackets) {
            String message = packet.getStrings().read(0);
            if (message.startsWith("/")) {
                redispatchCommand(event, player, message);
            } else {
                redispatchChatMessage(event, player, message);
            }
        }
    }

    private static void handleChatCommandPacket(PacketEvent event, Player player, PacketContainer packet) {
        if (InteractiveChat.forceUnsignedChatCommandPackets && packet.getModifier().size() > 3) {
            Object argumentSignature = packet.getModifier().read(3);
            if (ModernChatSigningUtils.isArgumentSignatureClass(argumentSignature) && !ModernChatSigningUtils.getArgumentSignatureEntries(argumentSignature).isEmpty()) {
                String command = "/" + packet.getStrings().read(0);
                redispatchCommand(event, player, command);
            }
        }
    }

    private static void redispatchCommand(PacketEvent event, Player player, String command) {
        event.setReadOnly(false);
        event.setCancelled(true);
        event.setReadOnly(true);
        Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
            PlayerUtils.dispatchCommandAsPlayer(player, command);
            if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                ModernChatSigningUtils.detectRateSpam(player, command);
            }
        });
    }

    private static void redispatchChatMessage(PacketEvent event, Player player, String message) {
        if (!ModernChatSigningUtils.isChatMessageIllegal(message)) {
            event.setReadOnly(false);
            event.setCancelled(true);
            event.setReadOnly(true);
            if (player.isConversing()) {
                Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.acceptConversationInput(message));
                if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                    Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> ModernChatSigningUtils.detectRateSpam(player, message));
                }
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
                    try {
                        Object decorated = ModernChatSigningUtils.getChatDecorator(player, LegacyComponentSerializer.legacySection().deserialize(message)).get();
                        PlayerUtils.chatAsPlayer(player, message, decorated);
                        if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                            ModernChatSigningUtils.detectRateSpam(player, message);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private static void handleServerDataPacket(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        if (event.getPacketType().equals(PacketType.Play.Server.SERVER_DATA)) {
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                if (InteractiveChat.hideServerUnsignedStatus && packet.getBooleans().size() > 0) {
                    packet.getBooleans().write(0, true);
                }
            } else {
                if (InteractiveChat.hideServerUnsignedStatus && packet.getBooleans().size() > 1) {
                    packet.getBooleans().write(1, true);
                }
            }
        }
    }
}
