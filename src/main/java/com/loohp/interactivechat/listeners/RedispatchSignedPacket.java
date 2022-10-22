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

package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class RedispatchSignedPacket {

    public static void packetListener() {
        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Client.CHAT_COMMAND, PacketType.Play.Client.CHAT)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                //do nothing
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.isPlayerTemporary() || event.isCancelled()) {
                    return;
                }
                if (!InteractiveChat.hasChatSigning()) {
                    return;
                }

                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                if (event.getPacketType().equals(PacketType.Play.Client.CHAT_COMMAND)) {
                    if (packet.getModifier().size() > 3) {
                        Object argumentSignature = packet.getModifier().read(3);
                        if (ModernChatSigningUtils.isArgumentSignatureClass(argumentSignature) && !ModernChatSigningUtils.getArgumentSignatureEntries(argumentSignature).isEmpty()) {
                            String command = packet.getStrings().read(0);
                            event.setReadOnly(false);
                            event.setCancelled(true);
                            event.setReadOnly(true);
                            Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
                                PlayerCommandPreprocessEvent playerCommandPreprocessEvent = new PlayerCommandPreprocessEvent(player, command);
                                Bukkit.getPluginManager().callEvent(playerCommandPreprocessEvent);
                                if (!playerCommandPreprocessEvent.isCancelled()) {
                                    Bukkit.dispatchCommand(player, playerCommandPreprocessEvent.getMessage());
                                }
                            });
                        }
                    }
                } else if (event.getPacketType().equals(PacketType.Play.Client.CHAT)) {
                    if (InteractiveChat.forceUnsignedChatPackets) {
                        String message = packet.getStrings().read(0);
                        event.setReadOnly(false);
                        event.setCancelled(true);
                        event.setReadOnly(true);
                        Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.chat(message));
                    }
                }
            }
        });

        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.HIGH).types(PacketType.Play.Server.SERVER_DATA)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isPlayerTemporary() || event.isCancelled()) {
                    return;
                }
                if (!InteractiveChat.hasChatSigning()) {
                    return;
                }

                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                if (event.getPacketType().equals(PacketType.Play.Server.SERVER_DATA)) {
                    if (InteractiveChat.hideServerUnsignedStatus) {
                        if (packet.getBooleans().size() > 1) {
                            packet.getBooleans().write(1, true);
                        }
                    }
                }
            }

        });
    }

}
