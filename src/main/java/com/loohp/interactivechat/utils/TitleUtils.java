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

package com.loohp.interactivechat.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.loohp.interactivechat.InteractiveChat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class TitleUtils {

    @SuppressWarnings("deprecation")
    public static void sendTitle(Player player, String title, String subtitle, String actionbar, int fadeIn, int stay, int fadeOut) {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            PacketContainer packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CLEAR_TITLES);
            packet1.getBooleans().write(0, true);

            PacketContainer packet2 = null;
            if (!title.equals("")) {
                try {
                    packet2 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SET_TITLE_TEXT);
                    packet2.getModifier().write(0, ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserialize(title), InteractiveChat.version.isLegacyRGB()));
                } catch (Exception e) {
                    e.printStackTrace();
                    packet2 = null;
                }
            }

            PacketContainer packet3 = null;
            if (!subtitle.equals("")) {
                try {
                    packet3 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SET_SUBTITLE_TEXT);
                    packet3.getModifier().write(0, ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserialize(subtitle), InteractiveChat.version.isLegacyRGB()));
                } catch (Exception e) {
                    e.printStackTrace();
                    packet3 = null;
                }
            }

            PacketContainer packet4 = null;
            if (!actionbar.equals("")) {
                try {
                    packet4 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
                    packet4.getModifier().write(0, ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserialize(actionbar), InteractiveChat.version.isLegacyRGB()));
                } catch (Exception e) {
                    e.printStackTrace();
                    packet4 = null;
                }
            }

            PacketContainer packet5 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SET_TITLES_ANIMATION);
            packet5.getIntegers().write(0, fadeIn);
            packet5.getIntegers().write(1, stay);
            packet5.getIntegers().write(2, fadeOut);

            try {
                InteractiveChat.protocolManager.sendServerPacket(player, packet1);
                if (packet2 != null) {
                    InteractiveChat.protocolManager.sendServerPacket(player, packet2);
                }
                if (packet3 != null) {
                    InteractiveChat.protocolManager.sendServerPacket(player, packet3);
                }
                if (packet4 != null) {
                    InteractiveChat.protocolManager.sendServerPacket(player, packet4);
                }
                InteractiveChat.protocolManager.sendServerPacket(player, packet5);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            PacketContainer packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
            packet1.getTitleActions().write(0, TitleAction.RESET);

            PacketContainer packet2 = null;
            if (!title.equals("")) {
                try {
                    packet2 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
                    packet2.getTitleActions().write(0, TitleAction.TITLE);
                    packet2.getModifier().write(1, ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserialize(title), InteractiveChat.version.isLegacyRGB()));
                } catch (Exception e) {
                    e.printStackTrace();
                    packet2 = null;
                }
            }

            PacketContainer packet3 = null;
            if (!subtitle.equals("")) {
                try {
                    packet3 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
                    packet3.getTitleActions().write(0, TitleAction.SUBTITLE);
                    packet3.getModifier().write(1, ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserialize(subtitle), InteractiveChat.version.isLegacyRGB()));
                } catch (Exception e) {
                    e.printStackTrace();
                    packet3 = null;
                }
            }

            PacketContainer packet4 = null;
            if (!actionbar.equals("")) {
                try {
                    packet4 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
                    packet4.getTitleActions().write(0, TitleAction.ACTIONBAR);
                    packet4.getModifier().write(1, ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserialize(actionbar), InteractiveChat.version.isLegacyRGB()));
                } catch (Exception e) {
                    e.printStackTrace();
                    packet4 = null;
                }
            }

            PacketContainer packet5 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
            packet5.getTitleActions().write(0, TitleAction.TIMES);
            packet5.getIntegers().write(0, fadeIn);
            packet5.getIntegers().write(1, stay);
            packet5.getIntegers().write(2, fadeOut);

            try {
                InteractiveChat.protocolManager.sendServerPacket(player, packet1);
                if (packet2 != null) {
                    InteractiveChat.protocolManager.sendServerPacket(player, packet2);
                }
                if (packet3 != null) {
                    InteractiveChat.protocolManager.sendServerPacket(player, packet3);
                }
                if (packet4 != null) {
                    InteractiveChat.protocolManager.sendServerPacket(player, packet4);
                }
                InteractiveChat.protocolManager.sendServerPacket(player, packet5);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

}
