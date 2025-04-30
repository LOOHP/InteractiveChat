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

package com.loohp.interactivechat.listeners.packet;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.platformscheduler.Scheduler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

/**
 * Reducing code duplication... one class at a time.
 * (where possible, of course!)
 */
public class RedispatchedSignPacketHandler {

    public static void redispatchCommand(Player player, String command) {
        Scheduler.runTask(InteractiveChat.plugin, () -> {
            PlayerUtils.dispatchCommandAsPlayer(player, command);
            if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                ModernChatSigningUtils.detectRateSpam(player, command);
            }
        }, player);
    }

    /**
     * Must check if ModernChatSigningUtils.isChatMessageIllegal is false!
     * @param player Player to dispatch the message as.
     * @param message Message to "re-dispatch"
     */
    public static void redispatchChatMessage(Player player, String message) {
        if (player.isConversing()) {
            Scheduler.runTask(InteractiveChat.plugin, () -> player.acceptConversationInput(message), player);
            if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                Scheduler.runTaskAsynchronously(InteractiveChat.plugin, () -> ModernChatSigningUtils.detectRateSpam(player, message));
            }
        } else {
            Scheduler.runTaskAsynchronously(InteractiveChat.plugin, () -> {
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
