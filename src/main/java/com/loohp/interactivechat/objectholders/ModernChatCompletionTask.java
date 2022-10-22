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

package com.loohp.interactivechat.objectholders;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.utils.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ModernChatCompletionTask {

    private static Object[] nmsClientboundCustomChatCompletionsPacketActions;

    static {
        if (isSupported()) {
            try {
                Class<?> nmsClientboundCustomChatCompletionsPacketActionClass = NMSUtils.getNMSClass("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket$a");
                nmsClientboundCustomChatCompletionsPacketActions = nmsClientboundCustomChatCompletionsPacketActionClass.getEnumConstants();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isSupported() {
        try {
            NMSUtils.getNMSClass("net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket");
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    public ModernChatCompletionTask() {
        if (!isSupported()) {
            throw new IllegalStateException("ModernChatCompletion is not supported on this server");
        }
        run();
    }

    private void run() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
            if (InteractiveChat.chatTabCompletionsEnabled) {
                for (Player tabCompleter : Bukkit.getOnlinePlayers()) {
                    List<String> tab = new ArrayList<>();

                    for (ICPlaceholder placeholder : InteractiveChat.placeholderList.values()) {
                        if (tabCompleter.hasPermission(placeholder.getPermission()) || (!placeholder.isBuildIn() && !InteractiveChat.useCustomPlaceholderPermissions)) {
                            tab.add(placeholder.getName());
                        }
                    }
                    for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                        if (!player.getUniqueId().equals(tabCompleter.getUniqueId())) {
                            tab.add(InteractiveChat.mentionPrefix + player.getName());
                            tab.add(InteractiveChat.mentionPrefix + player.getDisplayName());
                            for (String nickname : InteractiveChatAPI.getNicknames(player.getUniqueId())) {
                                tab.add(InteractiveChat.mentionPrefix + nickname);
                            }
                        }
                    }

                    PacketContainer chatCompletionPacket = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS);
                    chatCompletionPacket.getModifier().write(0, nmsClientboundCustomChatCompletionsPacketActions[2]);
                    chatCompletionPacket.getModifier().write(1, tab);

                    InteractiveChat.protocolManager.sendServerPacket(tabCompleter, chatCompletionPacket);
                }
            }
        }, 0, 10);
    }

}
