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

package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.PlayerUtils.ColorSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class ClientSettingPacket implements Listener {

    private static final Map<Player, Boolean> colorSettingsMap = new HashMap<>();

    public static ColorSettings getSettings(Player player) {
        Boolean settings = colorSettingsMap.get(player);
        return settings != null ? (settings ? ColorSettings.ON : ColorSettings.OFF) : ColorSettings.WAITING;
    }

    public static void clientSettingsListener() {
        Bukkit.getPluginManager().registerEvents(new ClientSettingPacket(), InteractiveChat.plugin);
        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Client.SETTINGS)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                //do nothing
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Client.SETTINGS)) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                boolean colorSettings = packet.getBooleans().read(0);
                ColorSettings originalColorSettings = getSettings(player);

                if ((originalColorSettings.equals(ColorSettings.WAITING) || originalColorSettings.equals(ColorSettings.ON)) && !colorSettings) {
                    Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.ColorsDisabled"))), 5);
                }

                if (originalColorSettings.equals(ColorSettings.OFF) && colorSettings) {
                    Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.ColorsReEnabled"))), 5);
                }

                colorSettingsMap.put(player, colorSettings);
            }
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        colorSettingsMap.remove(event.getPlayer());
    }

}
