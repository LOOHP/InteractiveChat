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

package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.OutboundPacket;
import com.loohp.platformscheduler.ScheduledTask;
import com.loohp.platformscheduler.Scheduler;

import java.util.function.LongSupplier;

public class ProtocolLibAsyncChatSendingExecutor extends AsyncChatSendingExecutor {

    public ProtocolLibAsyncChatSendingExecutor(LongSupplier executionWaitTime, long killThreadAfter) {
        super(executionWaitTime, killThreadAfter);
    }

    @Override
    public ScheduledTask packetSender() {
        return Scheduler.runTaskTimer(InteractiveChat.plugin, () -> {
            while (!sendingQueue.isEmpty()) {
                OutboundPacket out = sendingQueue.poll();
                try {
                    if (out.getReciever().isOnline()) {
                        ProtocolLibPlatform.protocolManager.sendServerPacket(out.getReciever(), (PacketContainer) out.getPacket(), false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1);
    }
}
