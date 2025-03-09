package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.OutboundPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public class ProtocolLibAsyncChatSendingExecutor extends AsyncChatSendingExecutor {
    public ProtocolLibAsyncChatSendingExecutor(LongSupplier executionWaitTime, long killThreadAfter) {
        super(executionWaitTime, killThreadAfter);
    }

    @Override
    public int packetSender() {
        return Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
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
        }, 0, 1).getTaskId();
    }
}
