package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.OutboundPacket;
import com.tcoded.folialib.wrapper.task.WrappedTask;

import java.util.function.LongSupplier;

public class ProtocolLibAsyncChatSendingExecutor extends AsyncChatSendingExecutor {
    public ProtocolLibAsyncChatSendingExecutor(LongSupplier executionWaitTime, long killThreadAfter) {
        super(executionWaitTime, killThreadAfter);
    }

    @Override
    public WrappedTask packetSender() {
        return InteractiveChat.plugin.getScheduler().runTimer(() -> {
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
