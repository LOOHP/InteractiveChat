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

package com.loohp.interactivechat.objectholders;

import com.loohp.interactivechat.InteractiveChat;
import com.tcoded.folialib.wrapper.task.WrappedTask;

import java.util.UUID;

public class MentionPair {

    private final UUID sender;
    private final UUID receiver;
    private final long timestamp;
    private final WrappedTask task;

    public MentionPair(UUID sender, UUID reciever) {
        this.sender = sender;
        this.receiver = reciever;
        this.timestamp = System.currentTimeMillis();
        this.task = run();
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getReciever() {
        return receiver;
    }

    public void remove() {
        task.cancel();
        InteractiveChat.mentionPair.remove(this);
    }

    private WrappedTask run() {
        return InteractiveChat.plugin.getScheduler().runTimer(() -> {
            if ((System.currentTimeMillis() - timestamp) > 3000) {
                task.cancel();
                InteractiveChat.mentionPair.remove(this);
            }
        }, 0, 5);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((receiver == null) ? 0 : receiver.hashCode());
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MentionPair)) {
            return false;
        }
        MentionPair other = (MentionPair) obj;
        if (receiver == null) {
            if (other.receiver != null) {
                return false;
            }
        } else if (!receiver.equals(other.receiver)) {
            return false;
        }
        if (sender == null) {
            if (other.sender != null) {
                return false;
            }
        } else if (!sender.equals(other.sender)) {
            return false;
        }
        if (task != other.task) {
            return false;
        }
        return timestamp == other.timestamp;
    }

}
