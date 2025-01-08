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

package com.loohp.interactivechat.objectholders;

import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.Sets;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.utils.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ModernChatCompletionTask implements Listener {

    public static boolean isSupported() {
        return NMS.getInstance().isCustomTabCompletionSupported();
    }

    private final Map<Player, Set<String>> registered;

    public ModernChatCompletionTask() {
        if (!isSupported()) {
            throw new IllegalStateException("ModernChatCompletion is not supported on this server");
        }
        this.registered = new ConcurrentHashMap<>();
        run();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        registered.remove(event.getPlayer());
    }

    private void run() {
        InteractiveChat.plugin.getScheduler().runTimerAsync((task) -> {
            if (InteractiveChat.chatTabCompletionsEnabled) {
                for (Player tabCompleter : Bukkit.getOnlinePlayers()) {
                    Set<String> tab = ConcurrentHashMap.newKeySet();

                    for (ICPlaceholder placeholder : InteractiveChat.placeholderList.values()) {
                        if (tabCompleter.hasPermission(placeholder.getPermission()) || (!placeholder.isBuildIn() && !InteractiveChat.useCustomPlaceholderPermissions)) {
                            tab.add(ChatColorUtils.stripColor(placeholder.getName()));
                        }
                    }
                    for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                        if (!player.isVanished() && !player.getUniqueId().equals(tabCompleter.getUniqueId())) {
                            tab.add(ChatColorUtils.stripColor(InteractiveChat.mentionPrefix + player.getName()));
                            tab.add(ChatColorUtils.stripColor(InteractiveChat.mentionPrefix + player.getDisplayName()));
                            for (String nickname : InteractiveChatAPI.getNicknames(player.getUniqueId())) {
                                tab.add(ChatColorUtils.stripColor(InteractiveChat.mentionPrefix + nickname));
                            }
                        }
                    }

                    Set<String> oldList = registered.computeIfAbsent(tabCompleter, k -> ConcurrentHashMap.newKeySet());

                    List<String> add = new ArrayList<>(Sets.difference(tab, oldList));
                    List<String> remove = new ArrayList<>(Sets.difference(oldList, tab));

                    if (!add.isEmpty()) {
                        PacketContainer chatCompletionPacket1 = NMS.getInstance().createCustomTabCompletionPacket(CustomTabCompletionAction.ADD, add);
                        InteractiveChat.protocolManager.sendServerPacket(tabCompleter, chatCompletionPacket1);
                    }

                    if (!remove.isEmpty()) {
                        PacketContainer chatCompletionPacket2 = NMS.getInstance().createCustomTabCompletionPacket(CustomTabCompletionAction.REMOVE, remove);
                        InteractiveChat.protocolManager.sendServerPacket(tabCompleter, chatCompletionPacket2);
                    }

                    remove.forEach(oldList::remove);
                    oldList.addAll(add);
                }
            }
        }, 0, 10);
    }

}
