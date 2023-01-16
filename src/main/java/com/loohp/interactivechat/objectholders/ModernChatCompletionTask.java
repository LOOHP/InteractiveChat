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
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ModernChatCompletionTask implements Listener {

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

    private final Map<Player, List<String>> registered;

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
        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
            if (InteractiveChat.chatTabCompletionsEnabled) {
                for (Player tabCompleter : Bukkit.getOnlinePlayers()) {
                    List<String> tab = new ArrayList<>();

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

                    List<String> oldList = registered.computeIfAbsent(tabCompleter, k -> new ArrayList<>());
                    List<String> add = tab.stream().filter(each -> !oldList.contains(each)).collect(Collectors.toList());
                    List<String> remove = oldList.stream().filter(each -> !tab.contains(each)).collect(Collectors.toList());
                    oldList.addAll(add);

                    PacketContainer chatCompletionPacket1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS);
                    chatCompletionPacket1.getModifier().write(0, nmsClientboundCustomChatCompletionsPacketActions[0]);
                    chatCompletionPacket1.getModifier().write(1, add);

                    PacketContainer chatCompletionPacket2 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS);
                    chatCompletionPacket2.getModifier().write(0, nmsClientboundCustomChatCompletionsPacketActions[1]);
                    chatCompletionPacket2.getModifier().write(1, remove);

                    InteractiveChat.protocolManager.sendServerPacket(tabCompleter, chatCompletionPacket1);
                    InteractiveChat.protocolManager.sendServerPacket(tabCompleter, chatCompletionPacket2);
                }
            }
        }, 0, 10);
    }

}
