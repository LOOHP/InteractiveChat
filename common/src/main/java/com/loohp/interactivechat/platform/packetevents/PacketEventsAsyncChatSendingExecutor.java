package com.loohp.interactivechat.platform.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.OutboundPacket;
import org.bukkit.Bukkit;

import java.util.function.LongSupplier;

public class PacketEventsAsyncChatSendingExecutor extends AsyncChatSendingExecutor {
    public PacketEventsAsyncChatSendingExecutor(LongSupplier executionWaitTime, long killThreadAfter) {
        super(executionWaitTime, killThreadAfter);
    }

    @Override
    public int packetSender() {
        return Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
            while (!sendingQueue.isEmpty()) {
                OutboundPacket out = sendingQueue.poll();
                try {
                    if (out.getReciever().isOnline()) {
                        PacketWrapper<?> wrapper = (PacketWrapper<?>) out.getPacket();

                        PacketEvents.getAPI().getPlayerManager().sendPacket(out.getReciever(), wrapper);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1).getTaskId();
    }
}
