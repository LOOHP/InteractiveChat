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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.platformscheduler.Scheduler;
import org.bukkit.entity.Player;

/**
 * Everything within the packet package (but not in sub-packages) contain platform independent code.
 * The sub-packages (packetevents and protocollib) contain the platform dependent code which will reference the methods within these classes.
 */
public class ClientSettingsHandler {

    /**
     * The ProtocolLib and PacketEvents independent packet listener classes call on this.
     * This is a simple method that checks the client settings for colour code configs.
     * It notifies the player about the disabling of colours, and sends a warning message concerning it.
     *
     * @param colorSettings Client side colour settings - whether colours are enabled or not
     * @param originalColorSettings Server side original colour settings. Compared with colorSettings with their original setting when they first joined.
     * @param player Player in question.
     */
    public static void handlePacketReceiving(boolean colorSettings, boolean originalColorSettings, Player player) {
        if (originalColorSettings && !colorSettings) {
            sendMessageLater(player, ConfigManager.getConfig().getString("Messages.ColorsDisabled"));
        } else if (!originalColorSettings && colorSettings) {
            sendMessageLater(player, ConfigManager.getConfig().getString("Messages.ColorsReEnabled"));
        }
    }

    private static void sendMessageLater(Player player, String message) {
        Scheduler.runTaskLater(InteractiveChat.plugin, () -> {
            player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', message));
        }, 5, player);
    }
}
