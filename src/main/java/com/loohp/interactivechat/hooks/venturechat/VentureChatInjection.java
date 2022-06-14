/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

package com.loohp.interactivechat.hooks.venturechat;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import mineverse.Aust1n46.chat.listeners.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class VentureChatInjection implements Listener {

    private static boolean init = false;
    private static PacketListener packetListener = null;

    public static void _init_() {
        Bukkit.getPluginManager().registerEvents(new VentureChatInjection(), InteractiveChat.plugin);
        InteractiveChat.protocolManager.getPacketListeners().forEach(each -> {
            if (each.getPlugin().getName().equals("VentureChat")) {
                ListeningWhitelist whitelist = each.getSendingWhitelist();
                if (whitelist.getTypes().stream().anyMatch(type -> {String name = type.name(); return name.equals("CHAT") || name.equals("SYSTEM_CHAT") || name.equals("CHAT_PREVIEW");})) {
                    if (whitelist.getPriority().equals(ListenerPriority.MONITOR)) {
                        InteractiveChat.protocolManager.removePacketListener(each);
                    }
                }
            }
        });
        init = true;
    }

    public static void firePacketListener(PacketEvent event) {
        if (packetListener == null) {
            packetListener = new PacketListener();
        }
        packetListener.onPacketSending(event);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (init) {
            if (event.getPlugin().getName().equalsIgnoreCase("VentureChat")) {
                Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
                    _init_();
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into VentureChat!");
                }, 100);
            }
        }
    }

}
