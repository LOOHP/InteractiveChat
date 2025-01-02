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

package com.loohp.interactivechat.hooks.essentials;

import com.earth2me.essentials.Essentials;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EssentialsNicknames implements Listener {

    private static final List<String> EMPTY_LIST = Collections.emptyList();
    private static final Map<UUID, List<String>> ESSENTIALS_NICK = new ConcurrentHashMap<>();
    private static Essentials essen;
    private static String prefix;

    public static void init() {
        essen = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        prefix = essen.getConfig().getString("nickname-prefix");

        InteractiveChatAPI.registerNicknameProvider(essen, uuid -> {
            if (InteractiveChat.useEssentialsNicknames) {
                List<String> names = ESSENTIALS_NICK.get(uuid);
                return names;
            } else {
                return EMPTY_LIST;
            }
        });

        InteractiveChat.plugin.getScheduler().runLater((task) -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                loadNicks(player);
            }
        }, 100);
    }

    public static void loadNicks(Player player) {
        if (essen.getUser(player.getUniqueId()).getNickname() != null && !essen.getUser(player.getUniqueId()).getNickname().equals("")) {
            String essentialsNick = essen.getUser(player.getUniqueId()).getNickname();
            List<String> names = new ArrayList<>();
            names.add(prefix + essentialsNick);
            ESSENTIALS_NICK.put(player.getUniqueId(), names);
        }
    }

    @EventHandler
    public void onEssentialsReload(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/essentials reload")) {
            if (event.getPlayer().hasPermission("essentials.essentials")) {
                InteractiveChat.plugin.getScheduler().runLater((task) -> {
                    prefix = essen.getConfig().getString("nickname-prefix");
                }, 40);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEssentialsNickChange(NickChangeEvent event) {
        try {
            List<String> names = new ArrayList<>();
            names.add(prefix + event.getValue());
            ESSENTIALS_NICK.put(event.getController().getBase().getUniqueId(), names);
        } catch (Exception ignore) {
        }
    }

    @EventHandler
    public void onEssentialsJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InteractiveChat.plugin.getScheduler().runLater((task) -> {
            loadNicks(player);
        }, 100);
    }

    @EventHandler
    public void onEssentialsLeave(PlayerQuitEvent event) {
        ESSENTIALS_NICK.remove(event.getPlayer().getUniqueId());
    }

}
