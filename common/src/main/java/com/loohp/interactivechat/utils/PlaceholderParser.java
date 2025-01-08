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

package com.loohp.interactivechat.utils;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import com.loohp.interactivechat.objectholders.ValuePairs;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderParser {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");

    static {
        InteractiveChat.plugin.getScheduler().runTimerAsync((task) -> {
            if (InteractiveChat.bungeecordMode) {
                if (InteractiveChat.useTooltipOnTab) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        parse(ICPlayerFactory.getICPlayer(player), InteractiveChat.tabTooltip);
                    }
                }
            }
        }, 100, 100);
    }

    public static String parse(OfflineICPlayer offlineICPlayer, String str) {
        if (InteractiveChat.parsePAPIOnMainThread && !Bukkit.isPrimaryThread()) {
            try {
                CompletableFuture<String> future = new CompletableFuture<>();
                InteractiveChat.plugin.getScheduler().runNextTick((task) -> future.complete(parse0(offlineICPlayer, str)));
                return future.get(1500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return "";
            } catch (TimeoutException e) {
                ICPlayer player = offlineICPlayer.getPlayer();
                if (player == null) {
                    return PlaceholderAPI.setPlaceholders(offlineICPlayer.getLocalOfflinePlayer(), str);
                } else {
                    if (player.isLocal()) {
                        return PlaceholderAPI.setPlaceholders(player.getLocalPlayer(), str);
                    } else {
                        return "";
                    }
                }
            }
        } else {
            return parse0(offlineICPlayer, str);
        }
    }

    private static String parse0(OfflineICPlayer offlineICPlayer, String str) {
        ICPlayer player = offlineICPlayer.getPlayer();
        if (player == null) {
            return PlaceholderAPI.setPlaceholders(offlineICPlayer.getLocalOfflinePlayer(), str);
        } else {
            if (player.isLocal()) {
                if (InteractiveChat.bungeecordMode) {
                    List<ValuePairs<String, String>> pairs = new ArrayList<>();
                    for (Entry<String, String> entry : getAllPlaceholdersContained(player.getLocalPlayer(), str).entrySet()) {
                        pairs.add(new ValuePairs<>(entry.getKey(), entry.getValue()));
                    }
                    try {
                        BungeeMessageSender.forwardPlaceholders(System.currentTimeMillis(), player.getUniqueId(), pairs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return PlaceholderAPI.setPlaceholders(player.getLocalPlayer(), str);
            } else {
                for (Entry<String, String> entry : player.getRemotePlaceholdersMapping().entrySet()) {
                    str = str.replace(entry.getKey(), entry.getValue());
                }
                return str;
            }
        }
    }

    public static Map<String, String> getAllPlaceholdersContained(Player player, String str) {
        Map<String, String> matchingPlaceholders = new HashMap<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
        while (matcher.find()) {
            String matching = matcher.group();
            matchingPlaceholders.put(matching, PlaceholderAPI.setPlaceholders(player, matching));
        }
        return matchingPlaceholders;
    }

}
