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

package com.loohp.interactivechat.proxy.objectholders;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

public class ProxyMessageForwardingHandler implements AutoCloseable {

    private final Map<UUID, Queue<ForwardMessageInfo>> messageOrder;
    private final Map<UUID, ForwardMessageInfo> messageData;
    private final Queue<OutboundMessage> sendingQueue;
    private final BiConsumer<ForwardMessageInfo, String> forwardForProcessing;
    private final BiConsumer<ForwardMessageInfo, String> sendToPlayer;
    private final Predicate<UUID> isPlayerOnline;
    private final Predicate<UUID> hasInteractiveChatOnConnectedServer;
    private final LongSupplier executionWaitTime;
    private final Map<UUID, Map<UUID, OutboundMessage>> waitingPackets;
    private final Map<UUID, Long> lastSuccessfulCheck;

    private final AtomicBoolean isValid;

    public ProxyMessageForwardingHandler(BiConsumer<ForwardMessageInfo, String> forwardForProcessing, BiConsumer<ForwardMessageInfo, String> sendToPlayer, Predicate<UUID> isPlayerOnline, Predicate<UUID> hasInteractiveChatOnConnectedServer, LongSupplier executionWaitTime) {
        this.isValid = new AtomicBoolean(true);
        this.messageOrder = new ConcurrentHashMap<>();
        this.messageData = new ConcurrentHashMap<>();
        this.sendingQueue = new ConcurrentLinkedQueue<>();
        this.forwardForProcessing = forwardForProcessing;
        this.sendToPlayer = sendToPlayer;
        this.isPlayerOnline = isPlayerOnline;
        this.hasInteractiveChatOnConnectedServer = hasInteractiveChatOnConnectedServer;
        this.executionWaitTime = executionWaitTime;
        this.waitingPackets = new ConcurrentHashMap<>();
        this.lastSuccessfulCheck = new ConcurrentHashMap<>();

        packetSender();
        packetOrderSender();
        monitor();
    }

    public synchronized void processMessage(UUID player, String message, int position, ChatPacketType type, Object originalPacket) {
        UUID messageId = UUID.randomUUID();
        if (hasInteractiveChatOnConnectedServer.test(player)) {
            messageOrder.putIfAbsent(player, new ConcurrentLinkedQueue<>());
            Queue<ForwardMessageInfo> queue = messageOrder.get(player);
            ForwardMessageInfo forwardMessageInfo = new ForwardMessageInfo(messageId, player, position, type, System.currentTimeMillis(), originalPacket);
            messageData.put(messageId, forwardMessageInfo);
            queue.add(forwardMessageInfo);
            forwardForProcessing.accept(forwardMessageInfo, message);
        } else {
            ForwardMessageInfo forwardMessageInfo = new ForwardMessageInfo(messageId, player, position, type, System.currentTimeMillis(), originalPacket);
            sendToPlayer.accept(forwardMessageInfo, message);
        }
    }

    public void receivedProcessedMessage(UUID messageId, String message) {
        ForwardMessageInfo info = messageData.remove(messageId);
        if (info != null) {
            Queue<ForwardMessageInfo> queue = messageOrder.get(info.getPlayer());
            OutboundMessage outboundMessage = new OutboundMessage(info, message);
            if (queue != null) {
                if (queue.contains(info)) {
                    waitingPackets.putIfAbsent(info.getPlayer(), new ConcurrentHashMap<>());
                    Map<UUID, OutboundMessage> waitingMap = waitingPackets.get(info.getPlayer());
                    waitingMap.put(messageId, outboundMessage);
                } else {
                    sendingQueue.add(outboundMessage);
                    queue.remove(info);
                }
            }
        }
    }

    public void clearPlayer(UUID player) {
        Queue<ForwardMessageInfo> messages = messageOrder.remove(player);
        if (messages != null) {
            for (ForwardMessageInfo info : messages) {
                messageData.remove(info.getId());
            }
        }
    }

    private void packetSender() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                while (!sendingQueue.isEmpty()) {
                    OutboundMessage out = sendingQueue.poll();
                    if (isPlayerOnline.test(out.getInfo().getPlayer())) {
                        sendToPlayer.accept(out.getInfo(), out.getMessage());
                    }
                }
            }
        }, 0, 50);
    }

    private void monitor() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                for (Queue<ForwardMessageInfo> queue : messageOrder.values()) {
                    queue.removeIf(each -> each.getTime() + executionWaitTime.getAsLong() < time);
                }
                messageData.values().removeIf(id -> id.getTime() + executionWaitTime.getAsLong() < time);

                for (UUID player : messageOrder.keySet()) {
                    if (!isPlayerOnline.test(player)) {
                        clearPlayer(player);
                    }
                }
            }
        }, 0, 1000);
    }

    private void packetOrderSender() {
        new Thread(() -> {
            while (true) {
                for (Entry<UUID, Map<UUID, OutboundMessage>> entry : waitingPackets.entrySet()) {
                    long time = System.currentTimeMillis();
                    UUID playerUUID = entry.getKey();
                    Queue<ForwardMessageInfo> orderingQueue = messageOrder.get(playerUUID);
                    Map<UUID, OutboundMessage> playerWaitingPackets = entry.getValue();
                    ForwardMessageInfo forwardMessageInfo;
                    if (orderingQueue != null) {
                        if ((forwardMessageInfo = orderingQueue.peek()) == null) {
                            Iterator<Entry<UUID, OutboundMessage>> itr = playerWaitingPackets.entrySet().iterator();
                            while (itr.hasNext()) {
                                sendingQueue.add(itr.next().getValue());
                                itr.remove();
                            }
                        } else {
                            UUID id = forwardMessageInfo.getId();
                            OutboundMessage outboundPacket = playerWaitingPackets.get(id);
                            if (outboundPacket != null) {
                                sendingQueue.add(outboundPacket);
                                playerWaitingPackets.remove(id);
                                orderingQueue.remove(forwardMessageInfo);
                                lastSuccessfulCheck.put(playerUUID, time);
                            } else {
                                if (playerWaitingPackets.isEmpty()) {
                                    lastSuccessfulCheck.put(playerUUID, time);
                                } else {
                                    Long lastSuccessful = lastSuccessfulCheck.get(playerUUID);
                                    if (lastSuccessful == null) {
                                        lastSuccessfulCheck.put(playerUUID, time);
                                    } else if ((lastSuccessful + executionWaitTime.getAsLong()) < time) {
                                        orderingQueue.poll();
                                        lastSuccessfulCheck.put(playerUUID, time);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isValid()) {
                    break;
                }
                try {
                    TimeUnit.NANOSECONDS.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }, "InteractiveChatProxy Async ChatPacket Ordered Sending Thread").start();
    }

    @Override
    public synchronized void close() throws Exception {
        isValid.set(false);
    }

    public boolean isValid() {
        return isValid.get();
    }

    public static class ForwardMessageInfo {

        private final UUID id;
        private final UUID player;
        private final int position;
        private final ChatPacketType type;
        private final long time;
        private final Object originalPacket;

        public ForwardMessageInfo(UUID id, UUID player, int position, ChatPacketType type, long time, Object originalPacket) {
            this.id = id;
            this.player = player;
            this.position = position;
            this.type = type;
            this.time = time;
            this.originalPacket = originalPacket;
        }

        public UUID getId() {
            return id;
        }

        public UUID getPlayer() {
            return player;
        }

        public int getPosition() {
            return position;
        }

        public ChatPacketType getType() {
            return type;
        }

        public long getTime() {
            return time;
        }

        public Object getOriginalPacket() {
            return originalPacket;
        }

    }

    private static class OutboundMessage {

        private final ForwardMessageInfo info;
        private final String message;

        public OutboundMessage(ForwardMessageInfo info, String message) {
            this.info = info;
            this.message = message;
        }

        public ForwardMessageInfo getInfo() {
            return info;
        }

        public String getMessage() {
            return message;
        }

    }

}
