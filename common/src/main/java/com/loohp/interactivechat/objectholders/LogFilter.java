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

package com.loohp.interactivechat.objectholders;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.modules.ProcessAccurateSender;
import com.loohp.interactivechat.modules.ProcessExternalMessage;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.platformscheduler.Scheduler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.UUID;

public class LogFilter implements Filter {

    private static final ThreadLocal<Boolean> FILTER_BYPASS = new ThreadLocal<>();

    public Filter.Result checkMessage(String message, Level level) {
        try {
            if (Boolean.TRUE.equals(FILTER_BYPASS.get()) || message == null || message.isEmpty()) {
                return Filter.Result.NEUTRAL;
            } else if (!Registry.ID_PATTERN.matcher(message).find() && !Registry.MENTION_TAG_CONVERTER.containsTags(message)) {
                return Filter.Result.NEUTRAL;
            } else {
                UUID senderUUID = ProcessAccurateSender.find(message);
                ICPlayer sender = senderUUID == null ? null : ICPlayerFactory.getICPlayer(senderUUID);
                if (InteractiveChat.bungeecordMode && sender != null && !sender.isLocal()) {
                    long delay = Math.max(1L, (long) Math.ceil(InteractiveChat.remoteDelay / 50.0));
                    Scheduler.runTaskLaterAsynchronously(InteractiveChat.plugin, () -> {
                        String processed;
                        try {
                            processed = ProcessExternalMessage.processWithoutReceiver(message);
                        } catch (Throwable e) {
                            processed = message;
                        }
                        log(level, processed);
                    }, delay);
                    return Filter.Result.DENY;
                }
                String processed = ProcessExternalMessage.processWithoutReceiver(message);
                log(level, processed);
                return Filter.Result.DENY;
            }
        } catch (Throwable e) {
            return Filter.Result.NEUTRAL;
        }
    }

    private void log(Level level, String message) {
        try {
            FILTER_BYPASS.set(true);
            LogManager.getRootLogger().log(level, message);
        } finally {
            FILTER_BYPASS.remove();
        }
    }

    public LifeCycle.State getState() {
        try {
            return LifeCycle.State.STARTED;
        } catch (Exception ignored) {
        }
        return null;
    }

    public void initialize() {

    }

    public boolean isStarted() {
        return true;
    }

    public boolean isStopped() {
        return false;
    }

    public void start() {

    }

    public void stop() {

    }

    public Filter.Result filter(LogEvent event) {
        return checkMessage(event.getMessage().getFormattedMessage(), event.getLevel());
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object... arg4) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Object message, Throwable arg4) {
        return checkMessage(message.toString(), arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Message message, Throwable arg4) {
        return checkMessage(message.getFormattedMessage(), arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12) {
        return checkMessage(message, arg1);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) {
        return checkMessage(message, arg1);
    }

    public Filter.Result getOnMatch() {
        return Filter.Result.NEUTRAL;
    }

    public Filter.Result getOnMismatch() {
        return Filter.Result.NEUTRAL;
    }

}
