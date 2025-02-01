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

package com.loohp.interactivechat.hooks.dynmap;

import com.loohp.interactivechat.InteractiveChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.dynmap.DynmapCore;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.common.DynmapListenerManager;
import org.dynmap.common.DynmapListenerManager.EventListener;
import org.dynmap.common.DynmapListenerManager.EventType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public class DynmapListener implements Listener {

    private static boolean init = false;

    @SuppressWarnings("unchecked")
    public static void _init_() {
        Bukkit.getPluginManager().registerEvents(new DynmapListener(), InteractiveChat.plugin);
        try {
            DynmapPlugin dynmapPlugin = DynmapPlugin.plugin;
            Field coreField = dynmapPlugin.getClass().getDeclaredField("core");
            coreField.setAccessible(true);
            DynmapCore dynmapCore = (DynmapCore) coreField.get(dynmapPlugin);
            coreField.setAccessible(false);
            DynmapListenerManager dynmapEvents = dynmapCore.listenerManager;
            Field listenerField = dynmapEvents.getClass().getDeclaredField("listeners");
            listenerField.setAccessible(true);
            Map<EventType, ArrayList<EventListener>> listeners = (Map<EventType, ArrayList<EventListener>>) listenerField.get(dynmapEvents);
            listenerField.setAccessible(false);
            listeners.remove(EventType.PLAYER_CHAT);
            dynmapEvents.addListener(EventType.PLAYER_CHAT, new DynmapCoreChatListener(dynmapCore));
            init = true;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (init) {
            if (event.getPlugin().getName().equalsIgnoreCase("dynmap")) {
                InteractiveChat.plugin.getScheduler().runLater((task) -> {
                    _init_();
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into Dynmap!");
                }, 100);
            }
        }
    }

}
