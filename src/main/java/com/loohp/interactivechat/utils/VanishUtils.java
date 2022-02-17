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

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactivechat.InteractiveChat;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VanishUtils {

    private static Object premiumVanishChatListener;
    private static Method premiumVanishChatListenerExecuteMethod;
    private static Set<UUID> offlineVanish = new HashSet<>();
    private static long cacheTimeout = 0;

    static {
        try {
            Plugin premiumVanish = Bukkit.getPluginManager().getPlugin("PremiumVanish");
            Class<?> premiumVanishChatListenerClass = Class.forName("de.myzelyam.premiumvanish.bukkit.listeners.ChatListener");
            premiumVanishChatListener = premiumVanishChatListenerClass.getConstructors()[0].newInstance(premiumVanish);
            premiumVanishChatListenerExecuteMethod = premiumVanishChatListenerClass.getMethod("execute", Listener.class, Event.class);
        } catch (Exception e) {
            premiumVanishChatListener = null;
            premiumVanishChatListenerExecuteMethod = null;
        }
    }

    public static Optional<String> checkChatIsCancelled(Player player, String message) {
        if (premiumVanishChatListener == null) {
            return Optional.of(message);
        } else {
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(!Bukkit.isPrimaryThread(), player, message, new HashSet<>());
            try {
                premiumVanishChatListenerExecuteMethod.invoke(premiumVanishChatListener, null, event);
                return event.isCancelled() ? Optional.empty() : Optional.of(event.getMessage());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                return Optional.of(message);
            }
        }
    }

    public static boolean isVanished(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        boolean isOnline = player != null;
        if (InteractiveChat.vanishHook) {
            if (isOnline) {
                if (VanishAPI.isInvisible(player)) {
                    return true;
                }
            } else {
                if (getOfflineVanish().contains(uuid)) {
                    return true;
                }
            }
        }
        if (InteractiveChat.cmiHook) {
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(uuid);
            if (user != null && user.isVanished()) {
                return true;
            }
        }
        if (InteractiveChat.essentialsHook) {
            Essentials ess3 = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            User user = ess3.getUser(uuid);
            return user != null && user.isVanished();
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private synchronized static Set<UUID> getOfflineVanish() {
        long time = System.currentTimeMillis();
        if (cacheTimeout < time) {
            offlineVanish = VanishAPI.getAllInvisiblePlayers().stream().collect(Collectors.toSet());
            cacheTimeout = time + 3000;
        }
        return offlineVanish;
    }

}
