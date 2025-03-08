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
    public void send(Object packet, Player player, UUID id) {
        // No need to cast to PacketContainer. packetSender() will cast to PacketContainer later, and this method's packet variable will always be a PacketContainer.
        // If someone is supplying something that *isn't* a PacketContainer, then it's layer 8.
        OutboundPacket outboundPacket = new OutboundPacket(player, packet);

        Queue<MessageOrderInfo> queue = messagesOrder.get(player.getUniqueId());
        if (queue == null) {
            sendingQueue.add(outboundPacket);
        } else {
            if (queue.stream().anyMatch(each -> each.getId().equals(id))) {
                waitingPackets.putIfAbsent(player.getUniqueId(), new ConcurrentHashMap<>());
                Map<UUID, OutboundPacket> waitingMap = waitingPackets.get(player.getUniqueId());
                waitingMap.put(id, outboundPacket);
            } else {
                sendingQueue.add(outboundPacket);
            }
        }
    }

    @Override
    public int packetSender() {
        return Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
            while (!sendingQueue.isEmpty()) {
                OutboundPacket out = sendingQueue.poll();
                try {
                    if (out.getReciever().isOnline()) {
                        InteractiveChat.protocolManager.sendServerPacket(out.getReciever(), (PacketContainer) out.getPacket(), false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1).getTaskId();
    }
}
