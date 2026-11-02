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

package com.loohp.interactivechat.objectholders;

import com.google.common.collect.Sets;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerCustomChatCompletionPacket;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.platformscheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
        Scheduler.runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
            // this timer runs async, so it can read a player from the online list just before
            // onQuit removes them and then re-add them via computeIfAbsent below. Nothing removed that entry
            // again, so the map kept the CraftPlayer (and its connection) alive forever. Drop every entry
            // whose key is no longer the current online Player object for that UUID.
            registered.keySet().removeIf(p -> Bukkit.getPlayer(p.getUniqueId()) != p);
            if (InteractiveChat.chatTabCompletionsEnabled) {
                Collection<? extends Player> tabCompleters = Bukkit.getOnlinePlayers();
                if (tabCompleters.isEmpty()) {
                    return;
                }

                // everything that does not depend on the receiving player is computed once per run
                // instead of once per (receiver x placeholder) / (receiver x online player). This used to be
                // O(players^2) stripColor calls (each a fresh Pattern.compile) plus O(players^2) nickname lookups
                // every 10 ticks. The resulting completion set per receiver is the same as before.
                Collection<ICPlaceholder> placeholderValues = InteractiveChat.placeholderList.values();
                List<ICPlaceholder> placeholders = new ArrayList<>(placeholderValues.size());
                List<String> placeholderNames = new ArrayList<>(placeholderValues.size());
                for (ICPlaceholder placeholder : placeholderValues) {
                    placeholders.add(placeholder);
                    placeholderNames.add(ChatColorUtils.stripColor(placeholder.getName()));
                }

                Collection<ICPlayer> icPlayers = ICPlayerFactory.getOnlineICPlayers();
                List<UUID> mentionableUUIDs = new ArrayList<>(icPlayers.size());
                List<List<String>> mentionableEntries = new ArrayList<>(icPlayers.size());
                for (ICPlayer player : icPlayers) {
                    if (!player.isVanished()) {
                        List<String> nicknames = InteractiveChatAPI.getNicknames(player.getUniqueId());
                        List<String> entries = new ArrayList<>(2 + nicknames.size());
                        entries.add(ChatColorUtils.stripColor(InteractiveChat.mentionPrefix + player.getName()));
                        entries.add(ChatColorUtils.stripColor(InteractiveChat.mentionPrefix + player.getDisplayName()));
                        for (String nickname : nicknames) {
                            entries.add(ChatColorUtils.stripColor(InteractiveChat.mentionPrefix + nickname));
                        }
                        mentionableUUIDs.add(player.getUniqueId());
                        mentionableEntries.add(entries);
                    }
                }

                for (Player tabCompleter : tabCompleters) {
                    if (Bukkit.getPlayer(tabCompleter.getUniqueId()) != tabCompleter) {
                        continue; // quit since the online list was taken, do not re-register
                    }
                    Set<String> tab = new HashSet<>();

                    for (int i = 0; i < placeholders.size(); i++) {
                        ICPlaceholder placeholder = placeholders.get(i);
                        if (tabCompleter.hasPermission(placeholder.getPermission()) || (!placeholder.isBuildIn() && !InteractiveChat.useCustomPlaceholderPermissions)) {
                            tab.add(placeholderNames.get(i));
                        }
                    }
                    UUID tabCompleterUUID = tabCompleter.getUniqueId();
                    for (int i = 0; i < mentionableUUIDs.size(); i++) {
                        if (!mentionableUUIDs.get(i).equals(tabCompleterUUID)) {
                            tab.addAll(mentionableEntries.get(i));
                        }
                    }

                    Set<String> oldList = registered.computeIfAbsent(tabCompleter, k -> ConcurrentHashMap.newKeySet());
                    if (Bukkit.getPlayer(tabCompleter.getUniqueId()) != tabCompleter) {
                        registered.remove(tabCompleter); // quit raced with the line above
                        continue;
                    }
                    if (tab.size() == oldList.size() && oldList.containsAll(tab)) {
                        continue; // unchanged, nothing to send (same outcome as empty add/remove diffs)
                    }

                    List<String> add = new ArrayList<>(Sets.difference(tab, oldList));
                    List<String> remove = new ArrayList<>(Sets.difference(oldList, tab));

                    if (!add.isEmpty()) {
                        PlatformPlayServerCustomChatCompletionPacket<?> packet = InteractiveChat.protocolPlatform.getPlatformPacketCreatorProvider().createPlayServerCustomChatCompletionPacket(CustomTabCompletionAction.ADD, add);
                        InteractiveChat.protocolPlatform.sendServerPacket(tabCompleter, packet);
                    }

                    if (!remove.isEmpty()) {
                        PlatformPlayServerCustomChatCompletionPacket<?> packet = InteractiveChat.protocolPlatform.getPlatformPacketCreatorProvider().createPlayServerCustomChatCompletionPacket(CustomTabCompletionAction.REMOVE, remove);
                        InteractiveChat.protocolPlatform.sendServerPacket(tabCompleter, packet);
                    }

                    remove.forEach(oldList::remove);
                    oldList.addAll(add);
                }
            }
        }, 0, 10);
    }

}
