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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.ClientSettingPacket;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.PermissionCache;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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

    private static Class<?> craftPlayerClass;
    private static Method craftPlayerGetHandleMethod;
    private static Field nmsPlayerPingField;
    private static Field nmsPlayerConnectionField;
    private static Method nmsPlayerConnectionChatMethod;
    private static Method nmsPlayerConnectionHandleCommandMethod;

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
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

        try {
            craftPlayerClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftPlayer");
            craftPlayerGetHandleMethod = craftPlayerClass.getMethod("getHandle");
            try {
                nmsPlayerPingField = craftPlayerGetHandleMethod.getReturnType().getField("ping");
            } catch (NoSuchFieldException ignore) {
            }
            nmsPlayerConnectionField = NMSUtils.reflectiveLookup(Field.class, () -> {
                return craftPlayerGetHandleMethod.getReturnType().getField("playerConnection");
            }, () -> {
                return craftPlayerGetHandleMethod.getReturnType().getField("b");
            });
            nmsPlayerConnectionChatMethod = Arrays.stream(nmsPlayerConnectionField.getType().getMethods()).filter(each -> each.getName().equals("chat")).findFirst().orElseThrow(() -> new ReflectiveOperationException());
            try {
                nmsPlayerConnectionHandleCommandMethod = nmsPlayerConnectionField.getType().getDeclaredMethod("handleCommand", String.class);
            } catch (NoSuchMethodException e) {
                nmsPlayerConnectionHandleCommandMethod = nmsPlayerConnectionField.getType().getDeclaredMethod("a", String.class);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static void chatAsPlayer(Player player, String message) {
        chatAsPlayer(player, message, null);
    }

    public static void chatAsPlayer(Player player, String message, Object unsignedContentOrResult) {
        if (Bukkit.isPrimaryThread()) {
            player.chat(message);
            return;
        }
        try {
            Object entityPlayer = craftPlayerGetHandleMethod.invoke(craftPlayerClass.cast(player));
            Object playerConnection = nmsPlayerConnectionField.get(entityPlayer);
            switch (nmsPlayerConnectionChatMethod.getParameterCount()) {
                case 2:
                    nmsPlayerConnectionChatMethod.invoke(playerConnection, message, true);
                    break;
                case 3:
                    Object playerChatMessage = ModernChatSigningUtils.getPlayerChatMessage(message);
                    if (unsignedContentOrResult != null) {
                        if (unsignedContentOrResult instanceof Component) {
                            playerChatMessage = ModernChatSigningUtils.withUnsignedContent(playerChatMessage, ChatComponentType.IChatBaseComponent.convertTo((Component) unsignedContentOrResult, false));
                        } else if (nmsPlayerConnectionChatMethod.getParameterTypes()[1].isInstance(unsignedContentOrResult)) {
                            playerChatMessage = unsignedContentOrResult;
                        } else if (ModernChatSigningUtils.hasWithResult()) {
                            playerChatMessage = ModernChatSigningUtils.withResult(playerChatMessage, unsignedContentOrResult);
                        } else {
                            throw new IllegalArgumentException("Unexpected type: " + unsignedContentOrResult.getClass());
                        }
                    }
                    nmsPlayerConnectionChatMethod.invoke(playerConnection, message, playerChatMessage, true);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + nmsPlayerConnectionChatMethod.getParameterCount());
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void dispatchCommandAsPlayer(Player player, String command) {
        if (!command.startsWith("/")) {
            throw new IllegalArgumentException("command must start with '/'");
        }
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("commands must only be dispatched on main thread");
        }
        try {
            nmsPlayerConnectionHandleCommandMethod.setAccessible(true);
            Object entityPlayer = craftPlayerGetHandleMethod.invoke(craftPlayerClass.cast(player));
            Object playerConnection = nmsPlayerConnectionField.get(entityPlayer);
            nmsPlayerConnectionHandleCommandMethod.invoke(playerConnection, command.trim());
        } catch (IllegalAccessException | InvocationTargetException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static int getPing(Player player) {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            return player.getPing();
        } else {
            try {
                Object entityPlayer = craftPlayerGetHandleMethod.invoke(craftPlayerClass.cast(player));
                return nmsPlayerPingField.getInt(entityPlayer);
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Integer.MAX_VALUE;
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
            Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
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

    public static ColorSettings getColorSettings(Player player) {
        return ClientSettingPacket.getSettings(player);
    }

    public static int getProtocolVersion(Player player) {
        int protocolVersion = -1;
        if (InteractiveChat.viaVersionHook) {
            protocolVersion = us.myles.ViaVersion.api.Via.getAPI().getPlayerVersion(player.getUniqueId());
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

    public enum ColorSettings {
        ON,
        OFF,
        WAITING
    }

}
