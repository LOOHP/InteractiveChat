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

package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClientSettingPacket {

    public static void clientSettingsListener() {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_2)) {
            InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Configuration.Client.CLIENT_INFORMATION)) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    //do nothing
                }

                @Override
                public void onPacketReceiving(PacketEvent event) {
                    handlePacketReceiving(event);
                }
            });
        } else {
            InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Client.SETTINGS)) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    //do nothing
                }

                @Override
                public void onPacketReceiving(PacketEvent event) {
                    handlePacketReceiving(event);
                }
            });
        }
    }

    public static void handlePacketReceiving(PacketEvent event) {
        if (event.isPlayerTemporary()) {
            return;
        }
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        boolean colorSettings = NMS.getInstance().getColorSettingsFromClientInformationPacket(packet);
        boolean originalColorSettings = PlayerUtils.canChatColor(player);
        if (originalColorSettings && !colorSettings) {
            InteractiveChat.plugin.getScheduler().runLater((task) -> player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.ColorsDisabled"))), 5);
        } else if (!originalColorSettings && colorSettings) {
            InteractiveChat.plugin.getScheduler().runLater((task) -> player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.ColorsReEnabled"))), 5);
        }
    }

}
