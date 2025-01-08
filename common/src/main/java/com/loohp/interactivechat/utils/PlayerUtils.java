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
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.PermissionCache;
import com.viaversion.viaversion.api.Via;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlayerUtils implements Listener {

    private static final Map<UUID, Map<String, PermissionCache>> PERMISSION_CACHE = new ConcurrentHashMap<>();

    static {
        InteractiveChat.plugin.getScheduler().runTimerAsync((task) -> {
            long now = System.currentTimeMillis();
            Iterator<Entry<UUID, Map<String, PermissionCache>>> itr0 = PERMISSION_CACHE.entrySet().iterator();
            while (itr0.hasNext()) {
                Entry<UUID, Map<String, PermissionCache>> entry = itr0.next();
                Map<String, PermissionCache> map = entry.getValue();
                if (map == null || map.isEmpty()) {
                    itr0.remove();
                } else {
                    map.values().removeIf(permissionCache -> permissionCache.getTime() + 180000 < now);
                }
            }
        }, 0, 600);
    }

    public static void chatAsPlayer(Player player, String message) {
        chatAsPlayer(player, message, null);
    }

    public static void chatAsPlayer(Player player, String message, Object unsignedContentOrResult) {
        if (Bukkit.isPrimaryThread()) {
            player.chat(message);
        } else {
            NMS.getInstance().chatAsPlayerAsync(player, message, unsignedContentOrResult);
        }
    }

    public static void dispatchCommandAsPlayer(Player player, String command) {
        NMS.getInstance().dispatchCommandAsPlayer(player, command);
    }

    public static int getPing(Player player) {
        return NMS.getInstance().getPing(player);
    }

    public static boolean hasPermission(UUID uuid, String permission, boolean def, int timeout) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            Map<String, PermissionCache> map = PERMISSION_CACHE.get(uuid);
            if (map == null) {
                PERMISSION_CACHE.putIfAbsent(uuid, new ConcurrentHashMap<>());
                map = PERMISSION_CACHE.get(uuid);
            }
            PermissionCache cachedResult = map.get(permission);
            boolean result = cachedResult != null ? cachedResult.getValue() : player.hasPermission(permission);
            if (cachedResult == null) {
                map.put(permission, new PermissionCache(result, System.currentTimeMillis()));
            } else {
                cachedResult.setValue(result);
            }
            return result;
        } else {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            InteractiveChat.plugin.getScheduler().runAsync((task) -> {
                Map<String, PermissionCache> map = PERMISSION_CACHE.get(uuid);
                if (map == null) {
                    PERMISSION_CACHE.putIfAbsent(uuid, new ConcurrentHashMap<>());
                    map = PERMISSION_CACHE.get(uuid);
                }
                PermissionCache cachedResult = map.get(permission);
                boolean result = cachedResult != null ? cachedResult.getValue() : InteractiveChat.perms.playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), permission);
                future.complete(result);
                if (cachedResult == null) {
                    map.put(permission, new PermissionCache(result, System.currentTimeMillis()));
                } else {
                    cachedResult.setValue(result);
                }
            });
            try {
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return def;
            }
        }
    }

    public static void resetAllPermissionCache() {
        PERMISSION_CACHE.clear();
    }

    public static void resetPermissionCache(UUID uuid) {
        PERMISSION_CACHE.remove(uuid);
    }

    public static ItemStack getHeldItem(Player player) {
        return getHeldItem(ICPlayerFactory.getICPlayer(player));
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getHeldItem(ICPlayer player) {
        ItemStack item;
        if (InteractiveChat.version.isOld()) {
            ItemStack held = player.getEquipment().getItemInHand();
            if (held == null || held.getType().equals(Material.AIR)) {
                item = new ItemStack(Material.AIR);
            } else {
                item = player.getEquipment().getItemInHand().clone();
            }
        } else {
            ItemStack held = player.getEquipment().getItemInMainHand();
            if (held == null || held.getType().equals(Material.AIR)) {
                item = new ItemStack(Material.AIR);
            } else {
                item = player.getEquipment().getItemInMainHand().clone();
            }
        }
        return item;
    }

    public static boolean canChatColor(Player player) {
        return NMS.getInstance().canChatColor(player);
    }

    public static int getProtocolVersion(Player player) {
        int protocolVersion = -1;
        if (InteractiveChat.viaVersionHook) {
            protocolVersion = Via.getAPI().getPlayerVersion(player.getUniqueId());
        } else if (InteractiveChat.protocolSupportHook) {
            protocolVersion = protocolsupport.api.ProtocolSupportAPI.getProtocolVersion(player).getId();
        } else {
            protocolVersion = InteractiveChat.protocolManager.getProtocolVersion(player);
        }
        return protocolVersion;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PERMISSION_CACHE.remove(event.getPlayer().getUniqueId());
    }

}
