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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.platform.PlatformPacketEvent;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.utils.ChatComponentType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MessagePacketHandler<PacketEvent, Packet> {

    public static final UUID UUID_NIL = new UUID(0, 0);
    public static final Executor SCHEDULING_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("InteractiveChat Async ChatMessage Process Scheduling Thread").build());
    public static final AsyncChatSendingExecutor SERVICE = new AsyncChatSendingExecutor(() -> (long) (InteractiveChat.bungeecordMode ? InteractiveChat.remoteDelay : 0) + 2000, 5000);

    public static AsyncChatSendingExecutor getAsyncChatSendingExecutor() {
        return SERVICE;
    }

    public static <PacketEvent> DeterminedSenderFinder<PacketEvent> undeterminedSender() {
        return event -> null;
    }

    private final PreFilter<PacketEvent> preFilter;
    private final DeterminedSenderFinder<PacketEvent> determinedSenderFunction;
    private final PacketAccessor<Packet> accessor;
    private final PacketWriter<Packet> writer;

    public MessagePacketHandler(PreFilter<PacketEvent> preFilter, DeterminedSenderFinder<PacketEvent> determinedSenderFunction, PacketAccessor<Packet> accessor, PacketWriter<Packet> writer) {
        this.preFilter = preFilter;
        this.determinedSenderFunction = determinedSenderFunction;
        this.accessor = accessor;
        this.writer = writer;
    }

    public MessagePacketHandler(PreFilter<PacketEvent> preFilter, PacketAccessor<Packet> accessor, PacketWriter<Packet> writer) {
        this(preFilter, undeterminedSender(), accessor, writer);
    }

    public PreFilter<PacketEvent> getPreFilter() {
        return preFilter;
    }

    public DeterminedSenderFinder<PacketEvent> getDeterminedSenderFunction() {
        return determinedSenderFunction;
    }

    public PacketAccessor<Packet> getAccessor() {
        return accessor;
    }

    public PacketWriter<Packet> getWriter() {
        return writer;
    }

    @FunctionalInterface
    public interface PreFilter<PacketEvent> {
        boolean test(PacketEvent event);

        @SuppressWarnings("unchecked")
        default boolean test(PlatformPacketEvent<?, ?, ?> event) {
            return test((PacketEvent) event.getHandle());
        }
    }

    @FunctionalInterface
    public interface DeterminedSenderFinder<PacketEvent> {
        ICPlayer apply(PacketEvent event);

        @SuppressWarnings("unchecked")
        default ICPlayer apply(PlatformPacketEvent<?, ?, ?> event) {
            return apply((PacketEvent) event.getHandle());
        }
    }

    @FunctionalInterface
    public interface PacketAccessor<Packet> {
        PacketAccessorResult apply(Packet packet, Player player);

        @SuppressWarnings("unchecked")
        default PacketAccessorResult apply(PlatformPlayServerUnifiedChatMessagePacket<?> packet, Player player) {
            return apply((Packet) packet.getHandle(), player);
        }
    }

    @FunctionalInterface
    public interface PacketWriter<Packet> {
        PacketWriterResult apply(Packet packet, Component component, ChatComponentType type, int field, UUID sender);

        @SuppressWarnings("unchecked")
        default PacketWriterResult apply(PlatformPlayServerUnifiedChatMessagePacket<?> packet, Component component, ChatComponentType type, int field, UUID sender) {
            return apply((Packet) packet.getHandle(), component, type, field, sender);
        }
    }

    public static class PacketAccessorResult {
        private final Component component;
        private final ChatComponentType type;
        private final int field;
        private final boolean preview;

        public PacketAccessorResult(Component component, ChatComponentType type, int field, boolean preview) {
            this.component = component;
            this.type = type;
            this.field = field;
            this.preview = preview;
        }

        public Component getComponent() {
            return component;
        }

        public ChatComponentType getType() {
            return type;
        }

        public int getField() {
            return field;
        }

        public boolean isPreview() {
            return preview;
        }
    }

    public static class PacketWriterResult {
        private final boolean tooLong;
        private final int jsonLength;
        private final UUID sender;

        public PacketWriterResult(boolean tooLong, int jsonLength, UUID sender) {
            this.tooLong = tooLong;
            this.jsonLength = jsonLength;
            this.sender = sender;
        }

        public boolean isTooLong() {
            return tooLong;
        }

        public int getJsonLength() {
            return jsonLength;
        }

        public UUID getSender() {
            return sender;
        }
    }

}
