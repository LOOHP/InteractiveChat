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
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.utils.ChatComponentType;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MessagePacketHandler {

    public static final UUID UUID_NIL = new UUID(0, 0);
    public static final Executor SCHEDULING_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("InteractiveChat Async ChatMessage Process Scheduling Thread").build());
    public static AsyncChatSendingExecutor SERVICE = null;

    public static AsyncChatSendingExecutor getAsyncChatSendingExecutor() {
        return SERVICE;
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
