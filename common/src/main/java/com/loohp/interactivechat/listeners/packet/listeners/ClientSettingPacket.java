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

package com.loohp.interactivechat.listeners.packet.listeners;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.ClientSettingsHandler;
import com.loohp.interactivechat.platform.PlatformPacketListenerPriority;
import com.loohp.interactivechat.platform.packets.PlatformConfigurationClientClientInformationPacket;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.platformscheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClientSettingPacket {

    public static void clientSettingsListener() {
        InteractiveChat.protocolPlatform.getPlatformPacketListenerProvider().listenToConfigurationClientClientInformation(InteractiveChat.plugin, PlatformPacketListenerPriority.MONITOR, event -> {
            if (event.isPlayerTemporary()) {
                return;
            }

            PlatformConfigurationClientClientInformationPacket<?> packet = event.getPacket();
            UUID uuid = event.getPlayerUniqueId();

            Scheduler.runTaskLater(InteractiveChat.plugin, () -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    boolean colorSettings = packet.getColorSettings();
                    boolean originalColorSettings = PlayerUtils.canChatColor(player);

                    ClientSettingsHandler.handlePacketReceiving(colorSettings, originalColorSettings, player);
                }
            }, 5);
        });
    }
}