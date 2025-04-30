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

package com.loohp.interactivechat.listeners;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.NativeAdventureConverter;
import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

public class PaperChatEvents implements Listener {

    private static Method getMessageMethod;
    private static Method setMessageMethod;
    private static Field originalMessageField;

    static {
        try {
            Method[] methods = AsyncChatEvent.class.getMethods();
            //noinspection OptionalGetWithoutIsPresent
            getMessageMethod = Arrays.stream(methods).filter(each -> each.getName().equals("message") && each.getParameterCount() == 0).findFirst().get();
            //noinspection OptionalGetWithoutIsPresent
            setMessageMethod = Arrays.stream(methods).filter(each -> each.getName().equals("message") && each.getParameterCount() == 1).findFirst().get();
            originalMessageField = AbstractChatEvent.class.getDeclaredField("originalMessage");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPaperChatLowest(AsyncChatEvent event) {
        checkSignedModificationsFromProxy(event);
        if (InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.LOWEST)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPaperChatLow(AsyncChatEvent event) {
        if (InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.LOW)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPaperChatNormal(AsyncChatEvent event) {
        if (InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.NORMAL)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPaperChatHigh(AsyncChatEvent event) {
        if (InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.HIGH)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPaperChatHighest(AsyncChatEvent event) {
        if (InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.HIGHEST)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPaperChatMonitor(AsyncChatEvent event) {
        if (InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.MONITOR)) {
            checkChat(event);
        }
    }

    public static void checkSignedModificationsFromProxy(AsyncChatEvent event) {
        AsyncPlayerChatEvent bukkitChatEvent = toBukkitChatEvent(event);
        ChatEvents.checkSignedModificationsFromProxy(bukkitChatEvent);
        applyBukkitChatEvent(event, bukkitChatEvent);
    }

    public static void checkChat(AsyncChatEvent event) {
        AsyncPlayerChatEvent bukkitChatEvent = toBukkitChatEvent(event);
        ChatEvents.checkChat(bukkitChatEvent);
        applyBukkitChatEvent(event, bukkitChatEvent);
    }

    public static AsyncPlayerChatEvent toBukkitChatEvent(AsyncChatEvent event) {
        return new AsyncPlayerChatEvent(event.isAsynchronous(), event.getPlayer(), InteractiveChatComponentSerializer.legacySection().serialize(NativeAdventureConverter.componentFromNative(getMessage(event))), Collections.emptySet());
    }

    public static void applyBukkitChatEvent(AsyncChatEvent event, AsyncPlayerChatEvent bukkitChatEvent) {
        setMessage(event, NativeAdventureConverter.componentToNative(InteractiveChatComponentSerializer.legacySection().deserialize(bukkitChatEvent.getMessage()), false));
        if (bukkitChatEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    public static Object getMessage(AsyncChatEvent event) {
        try {
            return getMessageMethod.invoke(event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setMessage(AsyncChatEvent event, Object component) {
        try {
            setMessageMethod.invoke(event, component);
            if (InteractiveChat.paperChatEventEditOriginalMessageField) {
                originalMessageField.setAccessible(true);
                originalMessageField.set(event, component);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
